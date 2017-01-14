package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.api.SimpleDependencyLifecycle;
import io.ibj.jsmc.api.*;
import io.ibj.jsmc.api.exceptions.*;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A dependency which is backed by a js script.
 *
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

    /**
     * Creates a new JsScript with a compiled script, dependency resolution scope, dependency resolver, and logger name
     *
     * @param compiledScript    Compiled script which is backing the JsScript
     * @param scope             Scope of the script
     * @param resolver          Resolver for this script
     * @param defaultLoggerName Default logger name for the script. This can be overwritten by the script
     */
    public JsScript(CompiledScript compiledScript, Scope scope, DependencyResolver<Scope> resolver, String defaultLoggerName) {
        if (compiledScript == null) throw new NullPointerException("compiledScript cannot be null");

        this.compiledScript = compiledScript;
        this.scope = scope;
        this.resolver = resolver;
        this.loggerName = defaultLoggerName;

        dependentLifecycleCache = new HashMap<>();
        dependencies = new HashSet<>();
    }

    private Map getInternalLifecycleObject() throws ModuleExecutionException {
        Map module = getModule();
        ScriptObjectMirror generator = (ScriptObjectMirror) module.get("generator");
        if (generator == null)
            return generateDefaultExports();
        return (Map) generator.call(null);
    }

    private Map generateDefaultExports() throws ModuleExecutionException {
        Map m = new HashMap();
        m.put("exports", getModule().get("exports"));
        return m;
    }

    private Map getModule() throws ModuleExecutionException {
        refreshInternalBindings();
        return (Map) internalBindings.get("module");
    }

    private void refreshInternalBindings() throws ModuleExecutionException {
        if (internalBindings != null) return;
        internalBindings = compiledScript.getEngine().createBindings();
        internalBindings.put("require", (Function<String, Object>) path -> {
            try {
                Optional<Dependency> dependencyResolve = resolver.resolve(scope, path);
                if (dependencyResolve.isPresent()) {
                    DependencyLifecycle dependencyLifecycle = dependencyResolve.get().depend(this);
                    dependencies.add(dependencyLifecycle);
                    return dependencyLifecycle.getDependencyExports();
                } else {
                    throw new DependencyResolutionFailedException(path);
                }
            } catch (ModuleCompilationException | ModuleExecutionException | IOException e) {
                throw new DependencyResolutionFailedException(path, e);
            }
        });
        HashMap<Object, Object> module = new HashMap<>();
        module.put("exports", new Object());
        internalBindings.put("module", module);
        SimpleScriptContext ctx = new SimpleScriptContext();
        ctx.setBindings(internalBindings, ScriptContext.ENGINE_SCOPE);
        try {
            compiledScript.eval(ctx);
        } catch (ScriptException e) {
            throw new ModuleExecutionException(e, "Exception occurred while executing script");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) throws ModuleExecutionException {
        DependencyLifecycle dl = dependentLifecycleCache.get(dependencyConsumer);
        if (dl == null) {
            Map lifecycleObject = getInternalLifecycleObject();
            Object exports = lifecycleObject.get("exports");
            dl = new SimpleDependencyLifecycle(this, exports, () -> {
                try {
                    Object closeFunction = lifecycleObject.get("close");
                    if (closeFunction instanceof ScriptObjectMirror) {
                        ((ScriptObjectMirror) closeFunction).call(null);
                    }
                } catch (Throwable t) {
                    report(new ModuleExecutionException(t, "An exception occurred while closing the script lifecycle"));
                }
                releaseLifecycle(dependencyConsumer);
            });
            dependentLifecycleCache.put(dependencyConsumer, dl);
        }
        return dl;
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
                    } catch (Throwable e) {
                        report(new ModuleExecutionException(e, "An exception occurred while disabling the module!"));
                    }
                }
            }
        }

        for (DependencyLifecycle dependency : dependencies) {
            try {
                dependency.close();
            } catch (Exception e) {
                report(new ModuleExecutionException(e, "Exception occurred while attempting to close dependency lifecycle!"));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DependencyConsumer> getDependents() {
        return dependentLifecycleCache.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Dependency> getDependencies() {
        return dependencies.stream().map(DependencyLifecycle::getParentDependency).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) throws ModuleNotFoundException, IOException, ModuleExecutionException, ModuleCompilationException {
        if (previouslyEvaluatedConsumers.contains(this)) return;
        previouslyEvaluatedConsumers.add(this);
        try {
            reevaluateDependents(previouslyEvaluatedConsumers);
        } catch (ModuleAlreadyLoadedException e) {
            throw new RuntimeException("ModuleAlreadyLoadedException thrown during a reevaluate", e);
        }
    }

    protected void reevaluateDependents(Collection<DependencyConsumer> previouslyEvaluatedConsumers) throws ModuleNotFoundException, ModuleExecutionException, ModuleCompilationException, ModuleAlreadyLoadedException, IOException {
        for (DependencyConsumer dependent : new HashSet<>(getDependents())) // avoid the CME, allow modules to reevaluate relationship.
            dependent.reevaluate(previouslyEvaluatedConsumers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void report(Throwable t) {
        if (internalBindings != null) {
            Object module = internalBindings.get("module");
            if (module instanceof Map) {
                Object onErrorHandler = ((Map) module).get("onError");
                if (onErrorHandler instanceof ScriptObjectMirror && ((ScriptObjectMirror) onErrorHandler).isFunction()) {
                    try {
                        ((ScriptObjectMirror) onErrorHandler).call(module, onErrorHandler);
                        return;
                    } catch (Throwable t2) {
                        logReportedException(new ModuleExecutionException(t2, "An exception occurred while attempting to handle the above exception!"));
                    }
                } // todo - should probably error instead of silently fail if errorHandler exist but isn't a function...
            }
        }
        logReportedException(t);
    }

    private void logReportedException(Throwable t) {
        getLogger().log(Level.SEVERE, "An unhandled exception occurred!", t);
    }

    /**
     * {@inheritDoc}
     */
    // todo - allow for scripts to define their own loggers, and cast at runtime
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
