package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.core.SimpleDependencyLifecycle;
import io.ibj.jsmc.api.*;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class JsScript<Scope> implements Dependency, DependencyConsumer, Reportable, Loggable {

    private final CompiledScript compiledScript;
    private final Scope scope;
    private final DependencyResolver<Scope> resolver;


    private final Map<DependencyConsumer, DependencyLifecycle> dependentLifecycleCache;
    private final Set<DependencyLifecycle> dependencies;

    private Logger logger;
    private String loggerName;

    private Bindings internalBindings;

    public JsScript(CompiledScript compiledScript, Scope scope, DependencyResolver<Scope> resolver, String defaultLoggerName) {
        this.compiledScript = compiledScript;
        this.scope = scope;
        this.resolver = resolver;
        this.loggerName = defaultLoggerName;

        dependentLifecycleCache = new HashMap<>();
        dependencies = new HashSet<>();
    }

    private Map getInternalLifecycleObject() {
        Map module = getModule();
        ScriptObjectMirror generator = (ScriptObjectMirror) module.get("generator");
        if (generator == null)
            return generateDefaultExports();
        return (Map) generator.call(null);
    }

    private Map generateDefaultExports() {
        Map m = new HashMap();
        m.put("exports", getModule().get("exports"));
        return m;
    }

    private Map getModule() {
        refreshInternalBindings();
        return (Map) internalBindings.get("module");
    }

    private void refreshInternalBindings() {
        if (internalBindings == null) {// must eval
            internalBindings = compiledScript.getEngine().createBindings();
            internalBindings.put("require", (Function<String, Object>) path -> {
                try {
                    Optional<Dependency> dependencyResolve = resolver.resolve(scope, path);
                    if (dependencyResolve.isPresent()) {
                        DependencyLifecycle dependencyLifecycle = dependencyResolve.get().depend(this);
                        dependencies.add(dependencyLifecycle);
                        return dependencyLifecycle.getDependencyExports();
                    } else {
                        throw new IllegalArgumentException("No dependent object found by identifier '" + path + "'.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error occurred during dependency resolution!", e);
                }
            });
            HashMap<Object, Object> module = new HashMap<>();
            module.put("exports", new Object());
            internalBindings.put("module", module);
            SimpleScriptContext ctx = new SimpleScriptContext();
            ctx.setBindings(internalBindings, ScriptContext.ENGINE_SCOPE);
            ctx.setAttribute(NashornScriptEngine.FILENAME, scope.toString(), ScriptContext.GLOBAL_SCOPE); // todo make real
            try {
                compiledScript.eval(ctx);
            } catch (Exception e) {
                report(new RuntimeException("Exception occurred while attempting to evaluate script!", e));
            }
        }
    }

    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) {
        return dependentLifecycleCache.computeIfAbsent(dependencyConsumer,
                dc -> {
                    Map lifecycleObject = getInternalLifecycleObject();
                    Object exports = lifecycleObject.get("exports");
                    return new SimpleDependencyLifecycle(this, exports, () -> {
                        try {
                            Object closeFunction = lifecycleObject.get("close");
                            if (closeFunction instanceof ScriptObjectMirror) {
                                ((ScriptObjectMirror) closeFunction).call(null);
                            }
                        } catch (Throwable t) {
                            report(new RuntimeException("An exception occurred while closing a dependency lifecycle!", t));
                        }
                        releaseLifecycle(dc);
                    });
                });
    }

    private void releaseLifecycle(DependencyConsumer consumer) {
        dependentLifecycleCache.remove(consumer);
        if (dependentLifecycleCache.isEmpty()) closeInternalLifecycle();
    }

    private void closeInternalLifecycle() {
        // first, call any module clean up
        if (internalBindings != null) {
            Object module = internalBindings.get("module");
            if (module != null) {
                Object disableFunction = ((Map) module).get("disable");
                if (disableFunction instanceof ScriptObjectMirror) {
                    try {
                        ((ScriptObjectMirror) disableFunction).call(module);
                    } catch (Exception e) {
                        report(new RuntimeException("An exception occurred while disabling the module!", e));
                    }
                }
            }
        }

        for (DependencyLifecycle dependency : dependencies) {
            try {
                dependency.close();
            } catch (Exception e) {
                report(new RuntimeException("Exception occurred while attempting to close dependency lifecycle!", e));
            }
        }
    }

    @Override
    public Collection<DependencyConsumer> getDependents() {
        return dependentLifecycleCache.keySet();
    }

    @Override
    public Collection<Dependency> getDependencies() {
        return dependencies.stream().map(DependencyLifecycle::getParentDependency).collect(Collectors.toSet());
    }

    @Override
    public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) {
        if (previouslyEvaluatedConsumers.contains(this)) return;
        previouslyEvaluatedConsumers.add(this);
        reevaluateDependents(previouslyEvaluatedConsumers);
    }

    protected void reevaluateDependents(Collection<DependencyConsumer> previouslyEvaluatedConsumers) {
        for (DependencyConsumer dependent : new HashSet<>(getDependents())) // avoid the CME, allow modules to reevaluate relationship.
            dependent.reevaluate(previouslyEvaluatedConsumers);
    }

    @Override
    public void report(Throwable t) {
        if (internalBindings != null) {
            Object module = internalBindings.get("module");
            if (module instanceof Map) {
                Object onErrorHandler = ((Map) module).get("onError");
                if (onErrorHandler instanceof ScriptObjectMirror && ((ScriptObjectMirror) onErrorHandler).isFunction()) {
                    try {
                        ((ScriptObjectMirror) onErrorHandler).call(module, onErrorHandler);
                    } catch (Throwable t2) {
                        logReportedException(t);
                        logReportedException(new RuntimeException("An exception occurred while attempting to handle the above exception!", t2));
                    }
                    return;
                }
            }
        }
        logReportedException(t);
    }

    private void logReportedException(Throwable t) {
        Logger l = this.getLogger();
        l.log(Level.SEVERE, "An unhandled exception occurred!");
        t.printStackTrace();
    }

    @Override
    public Logger getLogger() {
        if (internalBindings != null) {
            Object module = internalBindings.get("module");
            if (module instanceof Map) {
                Object loggerName = ((Map) module).get("loggerName");
                if (loggerName instanceof String) {
                    String moduleDefinedLoggerName = (String) loggerName;
                    if (!Objects.equals(moduleDefinedLoggerName, this.loggerName)) {
                        this.loggerName = moduleDefinedLoggerName;
                        this.logger = Logger.getLogger(this.loggerName);
                    }
                }
            }
        }

        if (logger == null)
            logger = Logger.getLogger(this.loggerName);

        return logger;
    }
}
