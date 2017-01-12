package io.ibj.jsmc.bukkit;

import io.ibj.jsmc.core.DependencyManager;
import io.ibj.jsmc.core.resolvers.FileSystemResolver;
import io.ibj.jsmc.core.resolvers.ModuleResolver;
import io.ibj.jsmc.core.resolvers.SystemDependencyResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

// todo - configurable default folder. May mean forcing instantiation of resolvers at 'onEnable'

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
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists())
            saveResource("config.yml", false);
        FileConfiguration c = new FileConfiguration();
        c.load(configFile);
        String loaderModuleName = c.getString("loader");
        // todo - catch exceptions
        try {
            dependencyManager.load(loaderModuleName); // this should then sequentially load everything else
        } catch (Exception e) {
            // todo - fail spectacularly. This is very bad if we can't load our module loader
            System.out.println("Exception occurred while enabling module '" + module + "':");
            e.printStackTrace();
        }
    }

    public static void logExceptionToPlugin(Plugin p, Throwable t) {
        // todo - more descriptive exceptions
        p.getLogger().log(Level.SEVERE, "An exception occurred!", t);
        t.printStackTrace();
    }
}
