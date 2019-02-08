package io.ibj.jsmc.bungee;

import io.ibj.jsmc.runtime.JsRuntime;
import io.ibj.jsmc.runtime.RuntimeHost;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class JsmcPlugin extends Plugin implements RuntimeHost {

    class BungeeJsRuntime extends JsRuntime {
        @Override
        protected InputStream getSeedPackage() {
            return getResourceAsStream("bungeePackage.zip");
        }
    }

    public final JsRuntime runtime;

    public JsmcPlugin() {
        runtime = new BungeeJsRuntime();
    }

    @Override
    public void onEnable() {
        Configuration c;
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                Files.copy(getResourceAsStream("bungeeConfig.yml"), configFile.toPath());
                getResourceAsStream("bungeeConfig.yml");
            }
            c = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE,
                    "jsmc was unable to load its configuration, or save a\n" +
                            "new default configuration. This was probably due to a\n" +
                            "filesystem persmissions issue.", e);
            return;
        }

        String loaderModuleName = c.getString("loader");
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
    public JsRuntime getRuntime() {
        return runtime;
    }
}
