package io.ibj.jsmc.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A dependency which is unable to pull dependents of its own, only permitted to be a dependency with a static export
 * object with no reporting on its dependents.
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public final class SystemDependency implements Dependency {

    private final Object export;
    private final Map<DependencyConsumer, DependencyLifecycle> lifecycleMap;

    /**
     * Creates a new dependency with the given export
     * @param export export
     */
    public SystemDependency(Object export) {
        this.export = export;
        lifecycleMap = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) {
        if (dependencyConsumer == null)
            throw new NullPointerException("Dependency consumer may not be null!");
        return lifecycleMap.computeIfAbsent(dependencyConsumer,
                dc -> new SimpleDependencyLifecycle(this, export, () -> lifecycleMap.remove(dc)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<DependencyConsumer> getDependents() {
        return lifecycleMap.keySet();
    }
}
