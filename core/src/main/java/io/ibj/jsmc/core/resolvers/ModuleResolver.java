package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Resolves dependencies at a module level, delegating all file operations to an internal file dependency resolver
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/9/16
 */
public class ModuleResolver implements DependencyResolver<Path> {

    // a valid module may only contain lower case letters, -, _, and numbers
    public static final Pattern validModulePattern = Pattern.compile("^([a-z-_0-9])*$");

    private final Path rootPath;
    private final DependencyResolver<Path> fileResolver;
    private final DependencyResolver<Path> downstreamResolver;

    /**
     * Creates a new module resolver with a root path, internal file resolver, and a fallback downstream resolver
     * @param rootPath root path of module resolution
     * @param fileResolver resolver used to look up files
     * @param downstreamResolver resolver used when this resolver fails
     */
    public ModuleResolver(Path rootPath, DependencyResolver<Path> fileResolver, DependencyResolver<Path> downstreamResolver) {
        this.rootPath = rootPath;
        this.fileResolver = fileResolver;
        this.downstreamResolver = downstreamResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Dependency> resolve(Path requestScope, String dependencyIdentifier) throws ModuleCompilationException, IOException {
        Optional<Dependency> d = Optional.empty();

        if (validModulePattern.matcher(dependencyIdentifier).matches()) {
            Path nodeModuleResolutionPath;
            if (Files.isDirectory(requestScope))
                nodeModuleResolutionPath = requestScope;
            else
                nodeModuleResolutionPath = requestScope.getParent();
            d = resolveWithinNodeModules(nodeModuleResolutionPath, dependencyIdentifier);
        }
        if (!d.isPresent() && downstreamResolver != null)
            return downstreamResolver.resolve(requestScope, dependencyIdentifier);
        return d;
    }

    private Optional<Dependency> resolveWithinNodeModules(Path currentDirectory, String identifier) throws ModuleCompilationException, IOException {
        // handles terminal case where .getParentFile() results in popping out of root!! (oh noez)
        if (currentDirectory == null) return Optional.empty();
        // handles when we are in a module directory. we can't search there, so we better pop out again
        while (currentDirectory.getFileName().endsWith("node_modules"))
            currentDirectory = currentDirectory.getParent();

        // if a file exists which points to a subdirectory 'node_modules', we should try to extract the module from it
        Path nodeModules = currentDirectory.resolve("node_modules");
        if (Files.isDirectory(nodeModules)) {
            Optional<Dependency> nodeModuleOptDep = fileResolver.resolve(nodeModules, "./" + identifier);
            if (nodeModuleOptDep.isPresent()) return nodeModuleOptDep;
        }
        // Detection of pop out of scope.
        if (Files.isSameFile(currentDirectory, rootPath))
            return Optional.empty();

        // try parent file system
        return resolveWithinNodeModules(currentDirectory.getParent(), identifier);
    }

    public Collection<String> getLoadableModules() throws IOException {
        Path nodeModules = rootPath.resolve("node_modules");
        if (!Files.isDirectory(nodeModules))
            return Collections.EMPTY_SET;

        Set<String> ret = new HashSet<>();

        Files.newDirectoryStream(nodeModules, path ->
                ModuleResolver.validModulePattern.matcher(path.getFileName().toString()).matches()
        ).forEach(p -> ret.add(p.getFileName().toString()));
        return ret;
    }
}
