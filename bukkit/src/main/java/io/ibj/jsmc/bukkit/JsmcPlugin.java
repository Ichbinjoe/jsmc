package io.ibj.jsmc.bukkit;

import io.ibj.jsmc.core.DependencyManager;
import io.ibj.jsmc.core.resolvers.FileSystemResolver;
import io.ibj.jsmc.core.resolvers.ModuleResolver;
import io.ibj.jsmc.core.resolvers.SystemDependencyResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/11/16
 */
public class JsmcPlugin extends JavaPlugin {

    private final FileSystemResolver fileSystemResolver;
    private final SystemDependencyResolver<File> systemDependencyResolver;
    private final SystemDependencyResolver<File> addOnDependencyResolver;
    private ModuleResolver moduleResolver = null;
    private final DependencyManager<File> dependencyManager;

    public JsmcPlugin() {
        fileSystemResolver = new FileSystemResolver(() -> moduleResolver);
        systemDependencyResolver = new SystemDependencyResolver<>(null);
        addOnDependencyResolver = new SystemDependencyResolver<>(systemDependencyResolver);
        moduleResolver = new ModuleResolver(Bukkit.getWorldContainer(), fileSystemResolver, addOnDependencyResolver);
        dependencyManager = new DependencyManager<>(moduleResolver, Bukkit.getWorldContainer());
    }

    @Override
    public void onDisable() {
        dependencyManager.unloadAll();
    }

    @Override
    public void onEnable() {
        for (String module : moduleResolver.getLoadableModules()) {
            try {
                dependencyManager.load(module);
            } catch (Exception e) {
                System.out.println("Exception occured while enabling module '" + module + "':");
                e.printStackTrace();
            }
        }
    }

    public static void logExceptionToPlugin(Plugin p, Throwable t) {
        p.getLogger().log(Level.SEVERE, "An exception occurred!", t);
        t.printStackTrace();
    }
}
