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
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * It loads javascript. Don't question it's power.
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class JsLoader {

    private static final NashornScriptEngine ENGINE = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
    private static final Lock ENGINE_LOCK = new ReentrantLock();

    private static final Gson GSON = new GsonBuilder().create();

    public static CompiledScript load(Reader r, String source) throws ScriptException {
        ENGINE_LOCK.lock();
        try {
            ENGINE.getContext().setAttribute(NashornScriptEngine.FILENAME, source, ScriptContext.ENGINE_SCOPE);
            return ENGINE.compile(r);
        } finally {
            ENGINE_LOCK.unlock();
        }
    }

    public static Object parseJson(Reader r) {
        return GSON.fromJson(r, Map.class);
    }

    public static Object parseJson(Path p) throws IOException {
        try (Reader r = Files.newBufferedReader(p)) {
            return parseJson(r);
        }
    }
}
