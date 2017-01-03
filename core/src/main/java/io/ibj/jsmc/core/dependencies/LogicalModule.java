package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.core.SimpleDependencyLifecycle;
import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyLifecycle;

import java.util.*;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class LogicalModule implements Dependency, DependencyConsumer {

    private Dependency internalMainDependency;
    private DependencyLifecycle internalLifecycle;
    private final Map<DependencyConsumer, DependencyLifecycle> dependencyLifecycleMap;

    public LogicalModule() {
        this.internalLifecycle = null;
        this.dependencyLifecycleMap = new HashMap<>();
    }

    public void setInternalDependency(Dependency dependency) {
        this.internalMainDependency = dependency;
    }

    private Object getInternalExports() {
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
            throw new RuntimeException("Exception occured while closing internal lifecycle!", e);
        }
        internalLifecycle = null;
    }

    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) {
        return dependencyLifecycleMap.computeIfAbsent(dependencyConsumer,
                dc -> new SimpleDependencyLifecycle(this, getInternalExports(), () -> releaseLifecycle(dc)));
    }

    @Override
    public Collection<DependencyConsumer> getDependents() {
        return dependencyLifecycleMap.keySet();
    }

    @Override
    public Collection<Dependency> getDependencies() {
        if (internalLifecycle != null) {
            List<Dependency> ret = new ArrayList<>(1);
            ret.add(internalMainDependency);
            return ret;
        } else
            return Collections.EMPTY_SET;
    }

    @Override
    public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) {
        if (previouslyEvaluatedConsumers.contains(this)) return;
        previouslyEvaluatedConsumers.add(this);
        closeInternalLifecycle();
        reevaluateDependents(previouslyEvaluatedConsumers);
    }

    protected void reevaluateDependents(Collection<DependencyConsumer> previouslyEvaluatedConsumers) {
        for (DependencyConsumer dependent : new HashSet<>(getDependents())) // avoid the CME, allow modules to reevaluate relationship.
            dependent.reevaluate(previouslyEvaluatedConsumers);
    }
}
