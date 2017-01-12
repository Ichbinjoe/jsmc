package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyResolver;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/9/16
 */
public class ModuleResolver implements DependencyResolver<File> {

    // a valid module may not have a /, \, space, or . in them
    private static final Pattern validModulePattern = Pattern.compile("^((?![/\\\\ .]).)*$");
    private static final Pattern moduleNameExtractionPattern = Pattern.compile("^(((?![/\\\\ .]).)*)((\\.js)|(\\.json))?$");

    private final File rootDir;
    private final DependencyResolver<File> fileResolver;
    private final DependencyResolver<File> downstreamResolver;

    public ModuleResolver(File rootDir, DependencyResolver<File> fileResolver, DependencyResolver<File> downstreamResolver) {
        this.rootDir = rootDir;
        this.fileResolver = fileResolver;
        this.downstreamResolver = downstreamResolver;
    }

    @Override
    public Optional<Dependency> resolve(File requestScope, String dependencyIdentifier) throws Exception {
        Optional<Dependency> d = Optional.empty();

        if (validModulePattern.matcher(dependencyIdentifier).matches()) {
            File f;
            if (requestScope.isDirectory())
                f = requestScope;
            else
                f = requestScope.getParentFile();
            d = resolveWithinNodeModules(f, dependencyIdentifier);
        }
        if (!d.isPresent() && downstreamResolver != null)
            return downstreamResolver.resolve(requestScope, dependencyIdentifier);
        return d;
    }

    private Optional<Dependency> resolveWithinNodeModules(File currentDirectory, String identifier) throws Exception {
        // handles terminal case where .getParentFile() results in popping out of root!! (oh noez)
        if (currentDirectory == null) return Optional.empty();
        // handles when we are in a module directory. we can't search there, so we better pop out again
        if (currentDirectory.getName().endsWith("node_modules"))
            return resolveWithinNodeModules(currentDirectory.getParentFile(), identifier);

        // if a file exists which points to a subdirectory 'node_modules', we should try to extract the module from it
        File nodeModules = new File(currentDirectory, "node_modules");
        if (nodeModules.isDirectory()) {
            Optional<Dependency> nodeModuleOptDep = fileResolver.resolve(nodeModules, "./" + identifier);
            if (nodeModuleOptDep.isPresent()) return nodeModuleOptDep;
        }
        // Detection of pop out of scope.
        // todo - is .toURI() the best way to do this? How do we achieve this effectively?
        if (rootDir.toURI().equals(currentDirectory.toURI()))
            return Optional.empty();

        // try parent file system
        return resolveWithinNodeModules(currentDirectory.getParentFile(), identifier);
    }

    public Collection<String> getLoadableModules() {
        File nodeModuleDir = new File(rootDir, "node_modules");
        if (!nodeModuleDir.isDirectory())
            return Collections.EMPTY_SET;

        Set<String> ret = new HashSet<>();

        // todo - should this include a list of all modules, or just modules which are capable of being 'loaded'/enabled at call
        for (File f : nodeModuleDir.listFiles()) {
            Matcher matcher = moduleNameExtractionPattern.matcher(f.getName());
            if (!matcher.matches()) continue;
            ret.add(matcher.group(1));
        }

        return ret;
    }
}
