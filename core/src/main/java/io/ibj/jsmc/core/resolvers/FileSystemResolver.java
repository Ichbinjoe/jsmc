package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.core.JsLoader;
import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyLifecycle;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.core.dependencies.JsScript;
import io.ibj.jsmc.core.dependencies.JsonDependency;
import io.ibj.jsmc.core.dependencies.LogicalModule;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class FileSystemResolver implements DependencyResolver<File> {

    private final Map<Path, Optional<Dependency>> cachedDependencies = new HashMap<>();
    private final Supplier<DependencyResolver<File>> pointDependencyResolver;

    public FileSystemResolver(Supplier<DependencyResolver<File>> pointDependencyResolver) {
        this.pointDependencyResolver = pointDependencyResolver;
    }

    @Override
    public Optional<Dependency> resolve(File requestScope, final String dependencyIdentifier) throws Exception {
        if (!(dependencyIdentifier.startsWith("./") || dependencyIdentifier.startsWith("../") || dependencyIdentifier.startsWith("/")))
            return Optional.empty();

        if (!requestScope.isDirectory())
            requestScope = requestScope.getParentFile(); // should operate on directories

        File requestedFile = new File(requestScope, dependencyIdentifier);
        Path p = requestedFile.toPath().toRealPath();
        return cachedDependencies.computeIfAbsent(p, uri -> {
            if (requestedFile.isDirectory()) {
                try {
                    return bootstrapDirectory(requestedFile);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load module '" + uri.toString() + "'!", e);
                }
            }
            if (dependencyIdentifier.endsWith(".js"))
                return resolve(uri, this::resolveJs);
            else if (dependencyIdentifier.endsWith(".json"))
                return resolve(uri, this::resolveJson);
            else {
                try {
                    Optional<Dependency> d = resolve(pathWithSubstring(requestedFile, ".js"), this::resolveJs);
                    if (d.isPresent()) return d;
                    return resolve(pathWithSubstring(requestedFile, ".json"), this::resolveJson);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Path pathWithSubstring(File base, String substring) throws IOException {
        return new File(base.getParentFile(), base.getName() + substring).toPath().toRealPath();
    }

    private Optional<Dependency> resolve(Path path, Function<Path, Optional<Dependency>> resolutionScheme) {
        return cachedDependencies.computeIfAbsent(path, resolutionScheme);
    }

    private Optional<Dependency> resolveJs(Path path) {
        File f = path.toFile();
        if (!f.exists()) return Optional.empty();
        try {
            return Optional.of(new JsScript<>(JsLoader.load(f), f, new PassthroughResolver<>(this, pointDependencyResolver.get()), f.getName()));
        } catch (IOException | ScriptException e) {
            throw new RuntimeException("Failed to load file '" + f.getAbsolutePath() + "'!", e);
        }
    }

    private Optional<Dependency> resolveJson(Path path) {
        File f = path.toFile();
        if (!f.exists()) return Optional.empty();
        try {
            return Optional.of(new JsonDependency(JsLoader.parseJson(f)));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load json zip entry '" + f.getName() + "'!", ex);
        }
    }

    private Optional<Dependency> bootstrapDirectory(File directory) throws Exception {
        // lets start resolving shit!
        LogicalModule module = new LogicalModule();
        // first, lets try and find a package.json we can read
        Optional<Dependency> packageJson = this.resolve(directory, "./package.json");
        if (packageJson.isPresent()) {
            try (DependencyLifecycle lifecycle = packageJson.get().depend(module)) {
                Map packageJsonContents = (Map) lifecycle.getDependencyExports();
                String main = (String) packageJsonContents.get("main");
                if (main == null)
                    throw new RuntimeException("Main js/json file not defined in package.json!");
                Optional<Dependency> mainDependency = this.resolve(directory, "./" + main);
                if (!mainDependency.isPresent())
                    throw new RuntimeException("Main js/json '" + main + "' does not exist!");
                module.setInternalDependency(mainDependency.get());
                return Optional.of(module);
            }
        }

        Optional<Dependency> indexJs = this.resolve(directory, "./index.js");
        if (indexJs.isPresent()) {
            module.setInternalDependency(indexJs.get());
            return Optional.of(module);
        }

        Optional<Dependency> indexJson = this.resolve(directory, "./index.json");
        if (indexJson.isPresent()) {
            module.setInternalDependency(indexJson.get());
            return Optional.of(module);
        }
        return Optional.empty(); // probably normal directory, not a module.
    }
}
