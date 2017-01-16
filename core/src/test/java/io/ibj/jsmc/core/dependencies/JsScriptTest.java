package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.api.*;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import io.ibj.jsmc.core.resolvers.SystemDependencyResolver;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.Data;
import org.junit.Test;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link JsScript}
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/15/17
 */
public class JsScriptTest extends DependencyContractTest {

    @Test
    public void testRequireInjectionReturnsDependencies() throws Exception {
        SystemDependencyResolver<Object> resolver = new SystemDependencyResolver<>(null);
        SystemDependency testDependency = new SystemDependency("3"); // yes, we are exporting 3.
        resolver.add("test", testDependency);

        CompiledScript internalScript = loadTestingResource("require.js");
        JsScript script = new JsScript(internalScript, null, resolver, null, false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);
        DependencyLifecycle lifecycle = script.depend(consumer);
        ScriptObjectMirror exports = ((ScriptObjectMirror) lifecycle.getDependencyExports());

        assertEquals("3", exports.get("result"));
    }

    @Test
    public void testRequireInjectionPassesScope() throws Exception {
        Object scope = new Object();
        SystemDependency testDependency = new SystemDependency("3");
        DependencyResolver<Object> resolver = mock(DependencyResolver.class);
        when(resolver.resolve(scope, "test")).thenReturn(Optional.of(testDependency));

        CompiledScript internalScript = loadTestingResource("require.js");
        JsScript script = new JsScript(internalScript, scope, resolver, null, false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);
        DependencyLifecycle lifecycle = script.depend(consumer);

        lifecycle.getDependencyExports(); // actually triggers script invocation

        verify(resolver, atLeastOnce()).resolve(scope, "test");
    }

    @Test
    public void testModuleExportsExposed() throws Exception {
        CompiledScript internalScript = loadTestingResource("module_exports_exposure.js");
        JsScript subject = new JsScript(internalScript, null, null, null, false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = subject.depend(consumer);

        ScriptObjectMirror exports = (ScriptObjectMirror) lifecycle.getDependencyExports();

        assertEquals("value", exports.get("key"));
    }

    @Data
    public static class CloseFlagTestHook {
        boolean closeCalled = false;
    }

    @Test
    public void testModuleGeneratorGeneratesAndCloses() throws Exception {
        SystemDependencyResolver<Object> resolver = new SystemDependencyResolver<>(null);
        CloseFlagTestHook testingHook = new CloseFlagTestHook();

        SystemDependency testDependency = new SystemDependency(testingHook); // yes, we are exporting 3.
        resolver.add("test", testDependency);

        CompiledScript internalScript = loadTestingResource("module_generator.js");

        JsScript subject = new JsScript(internalScript, null, resolver, null, false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = subject.depend(consumer);

        ScriptObjectMirror exports = (ScriptObjectMirror) lifecycle.getDependencyExports();

        assertEquals("value", exports.get("key"));
        assertFalse(testingHook.closeCalled);

        lifecycle.close();

        assertTrue(testingHook.closeCalled);
    }

    @Test
    public void testModuleDisableInvokedOnClose() throws Exception {
        SystemDependencyResolver<Object> resolver = new SystemDependencyResolver<>(null);
        CloseFlagTestHook testingHook = new CloseFlagTestHook();

        SystemDependency testDependency = new SystemDependency(testingHook);
        resolver.add("test", testDependency);

        CompiledScript internalScript = loadTestingResource("module_disable.js");

        JsScript subject = new JsScript(internalScript, null, resolver, null, false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = subject.depend(consumer);
        lifecycle.getDependencyExports();

        DependencyConsumer consumer2 = mock(DependencyConsumer.class);
        DependencyLifecycle lifecycle2 = subject.depend(consumer2);

        lifecycle.close();
        assertFalse(testingHook.closeCalled);
        lifecycle2.close();
        assertTrue(testingHook.closeCalled);
    }

    @Data
    public static class ReportedExceptionTestFlagHook {
        boolean exceptionReported = false;
    }

    @Test
    public void testReportOnErrorExceptionsOverrideThrowsEntireStack() throws Exception {
        SystemDependencyResolver<Object> resolver = new SystemDependencyResolver<>(null);
        ReportedExceptionTestFlagHook testingHook = new ReportedExceptionTestFlagHook();

        SystemDependency testDependency = new SystemDependency(testingHook);
        resolver.add("test", testDependency);

        CompiledScript internalScript = loadTestingResource("module_on_error_exception.js");

        JsScript subject = new JsScript(internalScript, null, resolver, null, false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = subject.depend(consumer);

        exception.expect(RuntimeException.class);
        lifecycle.close();
    }

    @Test
    public void testReportOnErrorExceptionsNormalReportsToLogger() throws Exception {
        SystemDependencyResolver<Object> resolver = new SystemDependencyResolver<>(null);
        ReportedExceptionTestFlagHook testingHook = new ReportedExceptionTestFlagHook();

        SystemDependency testDependency = new SystemDependency(testingHook);
        resolver.add("test", testDependency);

        CompiledScript internalScript = loadTestingResource("module_on_error_exception.js");

        JsScript subject = new JsScript(internalScript, null, resolver, "logger1", true);

        AtomicBoolean onErrorExceptionHandled = new AtomicBoolean(false);
        AtomicBoolean normalExceptionHandled = new AtomicBoolean(false);

        // shim into the java.util.logging system to block all records and detect if a log has been attempted
        Logger logger1 = Logger.getLogger("logger1");
        logger1.setLevel(Level.ALL);
        logger1.setFilter(record -> {
            Throwable t = record.getThrown();

            if (!(t instanceof ModuleExecutionException))
                return false;

            if (t.getMessage().equals("An exception occurred while attempting to handle an exception!")) {
                boolean hitAlready = onErrorExceptionHandled.getAndSet(true);
                if (hitAlready)
                    fail("onErrorException handled twice!");
            }

            if (t.getMessage().equals("An exception occurred while closing the script lifecycle")) {
                boolean hitAlready = normalExceptionHandled.getAndSet(true);
                if (hitAlready)
                    fail("normalException handled twice!");
            }
            return false;
        });

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = subject.depend(consumer);

        lifecycle.close();

        assertTrue(onErrorExceptionHandled.get());
        assertTrue(normalExceptionHandled.get());
    }

    @Test
    public void testExceptionalReporting() throws Exception {
        String[] testFiles = new String[]{"module_generator_exceptional_close.js", "module_exceptional_disable.js"};
        for (String testFile : testFiles) {
            SystemDependencyResolver<Object> resolver = new SystemDependencyResolver<>(null);
            ReportedExceptionTestFlagHook testingHook = new ReportedExceptionTestFlagHook();

            SystemDependency testDependency = new SystemDependency(testingHook);
            resolver.add("test", testDependency);

            CompiledScript internalScript = loadTestingResource(testFile);

            JsScript subject = new JsScript(internalScript, null, resolver, null, false);

            DependencyConsumer consumer = mock(DependencyConsumer.class);

            DependencyLifecycle lifecycle = subject.depend(consumer);

            lifecycle.getDependencyExports();

            lifecycle.close();

            assertTrue(testingHook.exceptionReported);
        }
    }

    @Test
    public void testLoggerNameOverrideToNewName() throws Exception {
        CompiledScript internalScript = loadTestingResource("module_logger_override.js");

        JsScript subject = new JsScript(internalScript, null, null, "defaultLogger", false);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = subject.depend(consumer);
        lifecycle.getDependencyExports();

        assertEquals("overrideLogger", subject.getLogger().getName());

    }

    @Override
    public Dependency createNewTestable() throws Exception {
        CompiledScript defaultCompiled = loadTestingResource("default.js");

        return new JsScript(defaultCompiled, null, null, "default.js", false);
    }

    private static final NashornScriptEngine ENGINE = ((NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine());

    private CompiledScript loadTestingResource(String identifier) throws IOException, ScriptException {
        try (Reader r = new FileReader(getTestingResource(identifier).getFile())) {
            return ENGINE.compile(r);
        }
    }

    private URL getTestingResource(String identifier) {
        ClassLoader classLoader = getClass().getClassLoader();
        return classLoader.getResource("JsScriptTest/" + identifier);
    }
}