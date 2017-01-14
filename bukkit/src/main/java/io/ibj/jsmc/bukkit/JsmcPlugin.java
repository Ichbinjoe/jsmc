package io.ibj.jsmc.bukkit;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyManager;
import io.ibj.jsmc.core.BasicDependencyManager;
import io.ibj.jsmc.core.resolvers.FileSystemResolver;
import io.ibj.jsmc.core.resolvers.ModuleResolver;
import io.ibj.jsmc.core.resolvers.SystemDependencyResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;

// todo - configurable default folder. May mean forcing instantiation of resolvers at 'onEnable'

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/11/16
 */
public class JsmcPlugin extends JavaPlugin {

    private static final String ERROR_HEADER = "=============================================\n";

    private final FileSystemResolver fileSystemResolver;
    private final SystemDependencyResolver<Path> systemDependencyResolver;
    private final SystemDependencyResolver<Path> addOnDependencyResolver;
    private ModuleResolver moduleResolver = null;
    private final BasicDependencyManager<Path> dependencyManager;

    public JsmcPlugin() {
        Path rootPath = Bukkit.getWorldContainer().toPath();
        fileSystemResolver = new FileSystemResolver(() -> moduleResolver, rootPath);
        systemDependencyResolver = new SystemDependencyResolver<>(null);
        addOnDependencyResolver = new SystemDependencyResolver<>(systemDependencyResolver);
        moduleResolver = new ModuleResolver(rootPath, fileSystemResolver, addOnDependencyResolver);
        dependencyManager = new BasicDependencyManager<>(moduleResolver, rootPath);
    }

    @Override
    public void onDisable() {
        try {
            for (DependencyManager.Entry e : dependencyManager.getLoadedModules())
                dependencyManager.unload(e);

        } catch (Exception e) {
            String msg = "" +
                    ERROR_HEADER +
                    "A severe exception has occurred! jsmc may not have shut down correctly.\n" +
                    "jsmc was unable to unload all of it's dependencies safely. This is usually\n" +
                    "not jsmc's fault, but instead a module on the system unable to shut down\n" +
                    "cleanly.\n" +
                    ERROR_HEADER;
            getLogger().log(Level.SEVERE, msg, e);
        }
    }

    @Override
    public void onEnable() {
        YamlConfiguration c;
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists())
                saveResource("config.yml", false);
            c = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            String msg = "" +
                    ERROR_HEADER +
                    "A severe exception has occurred! jsmc will not be able to load!\n" +
                    "jsmc was unable to read off/save a new config.yml in the jsmc\n" +
                    "plugin folder. This is usually due to file permissions or a\n" +
                    "malformed yaml.\n" +
                    "jsmc will now disable!\n" +
                    ERROR_HEADER;
            getLogger().log(Level.SEVERE, msg, e);
            setEnabled(false);
            return;
        }

        String loaderModuleName = c.getString("loader");
        if (loaderModuleName == null) {
            getLogger().info("loader null in config.yml, using mc-bukkit-default-loader...");
            loaderModuleName = "mc-bukkit-default-loader";
        }

        try {
            dependencyManager.load(loaderModuleName); // this should then sequentially load everything else
        } catch (Exception e) {
            String msg = "" +
                    ERROR_HEADER +
                    "A severe exception has occurred! jsmc will not be able to load!\n" +
                    "jsmc was unable to start the dependency management module:\n" + loaderModuleName + "\n" +
                    "jsmc will now disable!\n" +
                    ERROR_HEADER;
            getLogger().log(Level.SEVERE, msg, e);
            setEnabled(false);
            return;
        }
    }
}
