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

    private static final Pattern validModulePattern = Pattern.compile("^((?![\\/\\\\ .]).)*$");
    private static final Pattern moduleNameExtractionPattern = Pattern.compile("^(((?![\\/\\\\ .]).)*)((\\.js)|(\\.json)|(\\.zip))?$");

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
        if (currentDirectory == null) return Optional.empty();
        if (currentDirectory.getName().endsWith("node_modules"))
            return resolveWithinNodeModules(currentDirectory.getParentFile(), identifier);
        File nodeModules = new File(currentDirectory, "node_modules");
        if (nodeModules.isDirectory()) {
            Optional<Dependency> nodeModuleOptDep = fileResolver.resolve(nodeModules, "./" + identifier);
            if (nodeModuleOptDep.isPresent()) return nodeModuleOptDep;
        }
        if (rootDir.toURI().equals(currentDirectory.toURI()))
            return Optional.empty();
        return resolveWithinNodeModules(currentDirectory.getParentFile(), identifier);
    }

    public Collection<String> getLoadableModules() {
        File nodeModuleDir = new File(rootDir, "node_modules");
        if (!nodeModuleDir.isDirectory())
            return Collections.EMPTY_SET;

        Set<String> ret = new HashSet<>();

        for (File f : nodeModuleDir.listFiles()) {
            Matcher matcher = moduleNameExtractionPattern.matcher(f.getName());
            if (!matcher.matches()) continue;
            ret.add(matcher.group(1));
        }

        return ret;
    }
}
