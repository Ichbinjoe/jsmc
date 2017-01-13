package io.ibj.jsmc.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyLifecycle;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.core.dependencies.LogicalModule;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * It loads javascript. Don't question it's power.
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class JsLoader {

    private static final NashornScriptEngine ENGINE = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
    private static final Gson GSON = new GsonBuilder().create();

    public static CompiledScript load(Reader r) throws ScriptException {
        // todo - from what I recall, a script filename is set in this method (stupidly...) SHOWSTOPPER
        // this impacts the source of where the script is from, where we really need to point it at a filename. this is
        // a critical misdesign if it is - i need to be able to set a custom source!
        return ENGINE.compile(r);
    }

    public static CompiledScript load(Path p) throws IOException, ScriptException {
        try (Reader r = Files.newBufferedReader(p)) {
            return load(r);
        }
    }

    public static CompiledScript load(InputStream s) throws ScriptException, IOException {
        try (Reader r = new InputStreamReader(s)) {
            return load(r);
        }
    }

    public static Object parseJson(Reader r) {
        return GSON.fromJson(r, Map.class);
    }

    public static Object parseJson(InputStream s) throws IOException {
        try (Reader r = new InputStreamReader(s)) {
            return parseJson(r);
        }
    }

    public static Object parseJson(Path p) throws IOException {
        try (Reader r = Files.newBufferedReader(p)) {
            return parseJson(r);
        }
    }
}
