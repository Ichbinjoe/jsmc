package io.ibj.jsmc.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// todo - javadocs

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class SystemDependency implements Dependency {

    private final Object export;
    private final Map<DependencyConsumer, DependencyLifecycle> lifecycleMap;

    public SystemDependency(Object export) {
        this.export = export;
        lifecycleMap = new HashMap<>();
    }

    @Override
    public DependencyLifecycle depend(DependencyConsumer dependencyConsumer) {
        return lifecycleMap.computeIfAbsent(dependencyConsumer,
                dc -> new SimpleDependencyLifecycle(this, export, () -> lifecycleMap.remove(dc)));
    }

    @Override
    public Collection<DependencyConsumer> getDependents() {
        return lifecycleMap.keySet();
    }
}
