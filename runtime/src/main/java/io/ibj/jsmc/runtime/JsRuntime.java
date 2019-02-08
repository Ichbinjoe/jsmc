package io.ibj.jsmc.runtime;

import io.ibj.jsmc.api.DependencyManager;
import io.ibj.jsmc.api.exceptions.*;
import io.ibj.jsmc.core.BasicDependencyManager;
import io.ibj.jsmc.core.resolvers.FileSystemResolver;
import io.ibj.jsmc.core.resolvers.ModuleResolver;
import io.ibj.jsmc.core.resolvers.SystemDependencyResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * JsRuntime is the generic class containing common code for Bukkit, Bungeecord, Sponge, and Velocity. Due to the nature
 * of what plugins are actually responsible for, there is a lot of code sharing. Let's reduce that!
 */
public class JsRuntime {
    private final SystemDependencyResolver<Path> systemDependencyResolver;
    private final SystemDependencyResolver<Path> addOnDependencyResolver;

    private FileSystemResolver fileSystemResolver;
    private ModuleResolver moduleResolver;
    private BasicDependencyManager<Path> dependencyManager;

    public JsRuntime() {
        systemDependencyResolver = new SystemDependencyResolver<>(null);
        addOnDependencyResolver = new SystemDependencyResolver<>(systemDependencyResolver);
    }

    public void load(Path rootPath, String loader) throws IOException, ModuleExecutionException, ModuleCompilationException, ModuleAlreadyLoadedException, ModuleNotFoundException {
        Path nodeModulesPath = rootPath.resolve("node_modules");

        if (!Files.exists(nodeModulesPath)) {
            seedInstall(nodeModulesPath);
        } else {
            if (!Files.isDirectory(nodeModulesPath) && !(Files.isSymbolicLink(nodeModulesPath) && Files.isDirectory(nodeModulesPath.toRealPath()))) {
                throw new IllegalArgumentException("node_modules within the root directory is not a directory itself or a symlink to a directory.");
            }
        }

        fileSystemResolver = new FileSystemResolver(() -> moduleResolver, rootPath);
        moduleResolver = new ModuleResolver(rootPath, fileSystemResolver, addOnDependencyResolver);
        dependencyManager = new BasicDependencyManager<>(moduleResolver, rootPath);

        dependencyManager.load(loader);
    }

    public void unload() throws ModuleExecutionException, ModuleNotLoadedException {
        if (dependencyManager == null)
            return;

        for (DependencyManager.Entry e : dependencyManager.getLoadedModules())
            dependencyManager.unload(e);

        dependencyManager = null;
    }

    protected InputStream getSeedPackage() {
        return null;
    }

    private boolean seedInstall(Path nodeModulesPath) throws IOException {
        Files.createDirectory(nodeModulesPath);

        InputStream s = getSeedPackage();
        if (s == null)
            return false;

        try(ZipInputStream zis = new ZipInputStream(s)) {
            ZipEntry e;
            while((e = zis.getNextEntry()) != null) {
                Path child = nodeModulesPath.resolve(e.getName());
                if (e.isDirectory())
                    Files.createDirectory(child);
                else
                    Files.copy(zis, child);
            }
        }

        return true;
    }

}
