package io.ibj.jsmc.core.resolvers;

import com.google.gson.JsonParseException;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;
import io.ibj.jsmc.core.JsLoader;
import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyLifecycle;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.core.dependencies.JsScript;
import io.ibj.jsmc.core.dependencies.JsonDependency;
import io.ibj.jsmc.core.dependencies.LogicalModule;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
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
public class FileSystemResolver implements DependencyResolver<Path> {

    private interface Resolver {
        Optional<Dependency> apply(Path p) throws ModuleCompilationException, IOException;
    }

    private final Map<Path, Optional<Dependency>> cachedDependencies = new HashMap<>();
    private final Supplier<DependencyResolver<Path>> pointDependencyResolver;
    private final Path rootPath;

    public FileSystemResolver(Supplier<DependencyResolver<Path>> pointDependencyResolver, Path rootPath) {
        this.pointDependencyResolver = pointDependencyResolver;
        this.rootPath = rootPath;
    }

    @Override
    public Optional<Dependency> resolve(Path requestScope, final String dependencyIdentifier) throws ModuleCompilationException, IOException {
        // todo - should we even allow absolute paths? not sure if it is windows compatible (pretty sure it isn't)
        if (Files.notExists(requestScope)) throw new IllegalArgumentException("Request scope must exist");
        if (!(dependencyIdentifier.startsWith("./") || dependencyIdentifier.startsWith("../") || dependencyIdentifier.startsWith("/")))
            return Optional.empty();

        if (!Files.isDirectory(requestScope))
            requestScope = requestScope.getParent(); // we want to instead operate on directories

        Path requestPath = requestScope.resolve(dependencyIdentifier);
        return resolve(requestPath, p -> {
            if (Files.isDirectory(p))
                return bootstrapDirectory(p);
            else if (dependencyIdentifier.endsWith(".js"))
                return resolve(p, this::resolveJs);
            else if (dependencyIdentifier.endsWith(".json"))
                return resolve(p, this::resolveJson);
            else {
                Optional<Dependency> d = resolve(p.resolveSibling(p.getFileName() + ".js"), this::resolveJs);
                if (!d.isPresent())
                    return resolve(p.resolveSibling(p.getFileName() + ".json"), this::resolveJson);
                return d;
            }
        });
    }

    private Optional<Dependency> resolve(Path path, Resolver resolutionScheme) throws ModuleCompilationException, IOException {
        Optional<Dependency> dependency = cachedDependencies.get(path);
        if (dependency == null) {
            dependency = resolutionScheme.apply(path);
            cachedDependencies.put(path, dependency);
        }
        return dependency;
    }

    private Optional<Dependency> resolveJs(Path path) throws ModuleCompilationException, IOException {
        if (!Files.exists(path)) return Optional.empty();
        try (Reader r = Files.newBufferedReader(path)){
            return Optional.of(new JsScript<>(
                    JsLoader.load(r, path.relativize(rootPath).toString()),
                    path,
                    new PassthroughResolver<>(this, pointDependencyResolver.get()),
                    path.getFileName().toString()));
        } catch (ScriptException e) {
            throw new ModuleCompilationException(e, "Failed to compile script at '" + path.toAbsolutePath() + "'");
        }
    }

    private Optional<Dependency> resolveJson(Path path) throws ModuleCompilationException, IOException {
        if (!Files.exists(path)) return Optional.empty();
        try {
            return Optional.of(new JsonDependency(JsLoader.parseJson(path)));
        } catch (JsonParseException e) {
            throw new ModuleCompilationException(e, "Failed to parse json at '" + path.toAbsolutePath() + "'");
        }
    }

    /*
    Assumes that 'directory' exists and is a directory
     */
    private Optional<Dependency> bootstrapDirectory(Path directory) throws ModuleCompilationException, IOException {

        LogicalModule module = new LogicalModule();
        // first, lets try and find a package.json we can read
        Optional<Dependency> packageJson = this.resolve(directory, "./package.json");
        if (packageJson.isPresent()) {
            try (DependencyLifecycle lifecycle = packageJson.get().depend(module)) {
                Map packageJsonContents = (Map) lifecycle.getDependencyExports();
                String main = (String) packageJsonContents.get("main");
                if (main != null) {
                    Optional<Dependency> mainDependency = this.resolve(directory, "./" + main);
                    if (!mainDependency.isPresent())
                        throw new FileNotFoundException("'" + main + "' does not exist in the scope of package.json");
                    module.setInternalDependency(mainDependency.get());
                    return Optional.of(module);
                }
            } catch (ModuleCompilationException | IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("panic - json package close threw an exception!");
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
