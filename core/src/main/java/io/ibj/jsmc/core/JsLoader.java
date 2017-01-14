package io.ibj.jsmc.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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

    /**
     * Loads and compiles a script from the given reader with the passed source
     * @param r Reader to compile script from
     * @param source Source location to be reported by internal exceptions
     * @return Compiled script from reader with source
     * @throws ScriptException If the script fails to compile, or another assorted error
     */
    public static CompiledScript load(Reader r, String source) throws ScriptException {
        ENGINE_LOCK.lock();
        try {
            ENGINE.getContext().setAttribute(NashornScriptEngine.FILENAME, source, ScriptContext.ENGINE_SCOPE);
            return ENGINE.compile(r);
        } finally {
            ENGINE_LOCK.unlock();
        }
    }

    /**
     * Parses a reader using json into a dynamic object
     * @param r Reader to read json from
     * @return dynamic json object
     */
    public static Object parseJson(Reader r) {
        return GSON.fromJson(r, Map.class);
    }

    /**
     * Parses a file at the passed path using json into a dynamic object
     * @param p Path to read json from
     * @return dynamic json object
     * @throws IOException if an io based exception occurs
     */
    public static Object parseJson(Path p) throws IOException {
        try (Reader r = Files.newBufferedReader(p)) {
            return parseJson(r);
        }
    }
}
