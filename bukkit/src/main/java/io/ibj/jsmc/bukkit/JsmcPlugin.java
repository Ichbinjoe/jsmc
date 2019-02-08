package io.ibj.jsmc.bukkit;

import io.ibj.jsmc.runtime.JsRuntime;
import io.ibj.jsmc.runtime.RuntimeHost;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/11/16
 */
public class JsmcPlugin extends JavaPlugin implements RuntimeHost {
    class BukkitJsRuntime extends JsRuntime {
        @Override
        protected InputStream getSeedPackage() {
            return JsmcPlugin.this.getResource("bukkitPackage.zip");
        }
    }

    private final JsRuntime runtime;

    public JsmcPlugin() {
        runtime = new BukkitJsRuntime();
    }

    @Override
    public void onDisable() {
        try {
            this.runtime.unload();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE,
                    "An error occurred while disabling jsmc. This is usually \n" +
                            "due to a js module, not necessarily jsmc. As a result, \n" +
                            "may not have shut down all of its modules cleanly!", e);
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
            getLogger().log(Level.SEVERE,
                    "jsmc was unable to load its configuration, or save a\n" +
                            "new default configuration. This was probably due to a\n" +
                            "filesystem permissions issue.", e);
            setEnabled(false);
            return;
        }

        String loaderModuleName = c.getString("loader");
        if (loaderModuleName == null) {
            getLogger().info("loader null in config.yml, using mc-bukkit-default-loader...");
            loaderModuleName = "mc-bukkit-default-loader";
        }

        Path rootPath = new File(c.getString("root", "./")).toPath();

        try {
            runtime.load(rootPath, loaderModuleName);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE,
                    "jsmc encountered an error while trying to start its\n" +
                            "runtime. This is typically due to either a module or\n" +
                            "configuration error, not jsmc.", e);
        }
    }

    @Override
    public JsRuntime getRuntime() {
        return runtime;
    }
}
