package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.api.*;
import io.ibj.jsmc.api.exceptions.*;
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
 * <p>
 * Most operations are proxied to the script, with only some upper level 'management' operations being managed at this
 * level.
 * <p>
 * JS scripts are organized in a similar way to a 'node.js' module. Passed in are 2 structures:<br>
 * <code><br>
 * function require(module)<br>
 * + module = {<br>
 * exports: {}<br>
 * }<br>
 * </code>
 * <p>
 * These two objects are used to give the script all access rights in a similar way to how a node.js module operates.
 * <p>
 * <code>require()</code> delegates all calls to the passed in {@link DependencyResolver}. The actual behaviour of the
 * resolver is controlled external to this construct - attention should be given to how the object is constructed to
 * know the path which <code>require()</code> takes to resolve dependencies.
 * <p>
 * <code>module</code>is an object which encapsulates hooks and feedback to this class. Most prominently is the
 * <code>module.exports</code> object - similar to node.js, this object, (unless <code>module.generator</code> is
 * defined, information later) is used as the object which is passed through the {@link DependencyLifecycle} objects
 * returned by {@link JsScript#depend(DependencyConsumer)}) method to be used whenever this object is
 * <code>require()</code>'d.
 * <p>
 * <code>module.generator</code> is a jsmc specific addition, allowing for a 'generator' pattern to be applied to
 * modules - instead of defining a unilateral object as <code>module.exports</code>, modules are able to instead define
 * a generator which returns a specific object as well as a 'close' callback to be called when the object's lifecycle is
 * closed. This allows for modules to automatically perform cleanup pending a depending module's removal.
 * <code>module.generator</code> should return an object with at least the two following keys: <code>exports</code> and
 * <code>close</code>. <code>exports</code> is the object which will be exposed within the specific
 * {@link DependencyLifecycle}. <code>close</code> is a callback which is called (with no arguments) when the associated
 * {@link DependencyLifecycle} is closed.
 * If <code>module.generator</code> is not defined, the internal object will be <code>module.exports</code>, even if not
 * defined or null.
 * <p>
 * <p>
 * <code>module.disable</code> is a callback accepting 0 arguments which allows for a trigger when the module has no more
 * {@link DependencyLifecycle} references and needs to clean itself up. If it is not defined, then no special additional
 * action will be taken.
 * <p>
 * <code>module.onError</code> is a callback which accepts 1 argument being a {@link Throwable} which allows for a
 * module to report on its own errors. If not defined, errors are reported using the {@link Logger} returned by
 * {@link JsScript#getLogger()}.
 * <p>
 * <code>module.loggerName</code> is a string which defines the name of the logger returned by
 * {@link JsScript#getLogger()}. If it is not defined, the <code>defaultLoggerName</code> passed as a constructor
 * argument will be used instead.
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class JsScript<Scope> implements Dependency, DependencyConsumer, Reportable, Loggable {

    private final CompiledScript compiledScript;
    private final Scope scope;
    private final DependencyResolver<Scope> resolver;
    private final boolean reportOnErrorExceptions;

    private final Map<DependencyConsumer, DependencyLifecycle> dependentLifecycleCache;
    private final Set<DependencyLifecycle> dependencies;

    private Logger logger;
    private String loggerName;

    private Bindings internalBindings;

    /**
     * Creates a new JsScript with a compiled script, dependency resolution scope, dependency resolver, and logger name
     *
     * @param compiledScript          Compiled script which is backing the JsScript
     * @param scope                   Scope of the script
     * @param resolver                Resolver for this script
     * @param defaultLoggerName       Default logger name for the script. This can be overwritten by the script
     * @param reportOnErrorExceptions Whether or not to report exceptions thrown in module.onError. This should be true
     *                                for production, and false for unit testing (so JUnit exceptions can get through)
     */
    public JsScript(CompiledScript compiledScript, Scope scope, DependencyResolver<Scope> resolver,
                    String defaultLoggerName, boolean reportOnErrorExceptions) {
        if (compiledScript == null) throw new NullPointerException("compiledScript cannot be null");

        this.compiledScript = compiledScript;
        this.scope = scope;
        this.resolver = resolver;
        this.loggerName = defaultLoggerName;
        this.reportOnErrorExceptions = reportOnErrorExceptions;

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
        if (dependencyConsumer == null)
            throw new NullPointerException("dependencyConsumer cannot be null");

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
     * <p>
     * Reporting first attempts to pass the {@link Throwable} to module.onError, if it is defined. If module.onError,
     * both the passed throwable as well as the exception generated by the script (wrapped in a
     * {@link ModuleExecutionException}) will be reported to the logger returned by {@link JsScript#getLogger()}
     * <p>
     * If module.onError is not defined, the throwable is reported to the logger returned by {@link JsScript#getLogger()}
     */
    @Override
    public void report(Throwable t) {
        if (internalBindings != null) {
            Object module = internalBindings.get("module");
            if (module instanceof Map) {
                Object onErrorHandler = ((Map) module).get("onError");
                if (onErrorHandler instanceof ScriptObjectMirror && ((ScriptObjectMirror) onErrorHandler).isFunction()) {
                    try {
                        ((ScriptObjectMirror) onErrorHandler).call(module, t);
                        return;
                    } catch (Throwable t2) {
                        if (!reportOnErrorExceptions)
                            throw t2; // rethrow, as we shouldn't report double errors (unit testing)
                        logReportedException(new ModuleExecutionException(t2, "An exception occurred while attempting to handle an exception!"));
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
     * <p>
     * Logger name is defined by the following criteria:
     * <p>
     * 1) if module.loggerName is defined by the script, then that is the name of the logger used
     * 2) if module.loggerName is not defined, then the defaultLoggerName defined in the constructor should be used
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
