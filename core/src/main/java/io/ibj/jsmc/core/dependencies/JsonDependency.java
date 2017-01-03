package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.core.SimpleDependencyLifecycle;
import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyLifecycle;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/9/16
 */
public class JsonDependency implements Dependency {

    private final Object object;
    private final Map<DependencyConsumer, DependencyLifecycle> dependentLifecycleCache;

    public JsonDependency(Object object) {
        this.object = object;
        dependentLifecycleCache = new HashMap<>();
    }

    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) {
        return dependentLifecycleCache.computeIfAbsent(dependencyConsumer,
                dc -> new SimpleDependencyLifecycle(this, object, () -> releaseLifecycle(dc)));
    }

    private void releaseLifecycle(DependencyConsumer consumer) {
        dependentLifecycleCache.remove(consumer);
    }

    @Override
    public Collection<DependencyConsumer> getDependents() {
        return dependentLifecycleCache.keySet();
    }
}
