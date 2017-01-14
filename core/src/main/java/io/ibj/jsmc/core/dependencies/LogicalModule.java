package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.api.*;
import io.ibj.jsmc.api.exceptions.ModuleAlreadyLoadedException;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import io.ibj.jsmc.api.exceptions.ModuleNotFoundException;

import java.io.IOException;
import java.util.*;

/**
 * A logical module which delegates its lifecycle state to an internal dependency that is set post-creation
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class LogicalModule implements Dependency, DependencyConsumer {

    private Dependency internalMainDependency;
    private DependencyLifecycle internalLifecycle;
    private final Map<DependencyConsumer, DependencyLifecycle> dependencyLifecycleMap;

    /**
     * Creates a new logical module with no internalLifecycle (needs set!)
     */
    public LogicalModule() {
        this.internalLifecycle = null;
        this.dependencyLifecycleMap = new HashMap<>();
    }

    /**
     * Sets the internal dependency of the module.
     * @param dependency internal dependency to set
     * @throws IllegalStateException if internalMainDependency is already set
     */
    public void setInternalDependency(Dependency dependency) {
        if (internalMainDependency != null) throw new IllegalStateException("setInternalDependency called twice!");
        this.internalMainDependency = dependency;
    }

    private Object getInternalExports() throws ModuleExecutionException {
        if (internalLifecycle == null)
            internalLifecycle = internalMainDependency.depend(this);
        return internalLifecycle.getDependencyExports();
    }

    private void releaseLifecycle(DependencyConsumer consumer) {
        dependencyLifecycleMap.remove(consumer);
        if (dependencyLifecycleMap.isEmpty()) closeInternalLifecycle();
    }

    private void closeInternalLifecycle() {
        try {
            internalLifecycle.close();
        } catch (Exception e) {
            Throwable mee = new ModuleExecutionException(e, "Exception occurred while closing an internal lifecycle!");
            if (internalLifecycle.getParentDependency() instanceof Reportable)
                ((Reportable) internalLifecycle.getParentDependency()).report(mee);
            else // todo - could we not throw a runtime exception here?
                throw new RuntimeException("Failed to close internal lifecycle, and could not report. Fatal!", mee);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) throws ModuleExecutionException {
        DependencyLifecycle dl = dependencyLifecycleMap.get(dependencyConsumer);
        if (dl == null) {
            dl = new SimpleDependencyLifecycle(this, getInternalExports(), () -> releaseLifecycle(dependencyConsumer));
            dependencyLifecycleMap.put(dependencyConsumer, dl);
        }
        return dl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DependencyConsumer> getDependents() {
        return dependencyLifecycleMap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Dependency> getDependencies() {
        if (internalLifecycle != null) {
            List<Dependency> ret = new ArrayList<>(1);
            ret.add(internalMainDependency);
            return ret;
        } else
            return Collections.EMPTY_SET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) throws ModuleNotFoundException, IOException, ModuleExecutionException, ModuleCompilationException, ModuleAlreadyLoadedException {
        if (previouslyEvaluatedConsumers.contains(this)) return;
        previouslyEvaluatedConsumers.add(this);
        closeInternalLifecycle();
        reevaluateDependents(previouslyEvaluatedConsumers);
    }

    protected void reevaluateDependents(Collection<DependencyConsumer> previouslyEvaluatedConsumers) throws ModuleNotFoundException, ModuleExecutionException, ModuleCompilationException, ModuleAlreadyLoadedException, IOException {
        for (DependencyConsumer dependent : new HashSet<>(getDependents())) // avoid the CME, allow modules to reevaluate relationship.
            dependent.reevaluate(previouslyEvaluatedConsumers);
    }
}
