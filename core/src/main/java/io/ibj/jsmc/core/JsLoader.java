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
        return ENGINE.compile(r);
    }

    public static CompiledScript load(File f) throws IOException, ScriptException {
        try (Reader r = new FileReader(f)) {
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

    public static Object parseJson(File f) throws IOException {
        try (Reader r = new FileReader(f)) {
            return parseJson(r);
        }
    }
}
