package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyResolver;

import java.util.*;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class SystemDependencyResolver<Scope> implements DependencyResolver<Scope> {

    private final Map<String, Dependency> systemDependencyMap;
    private final DependencyResolver<Scope> parentDependencyResolver;

    public SystemDependencyResolver(DependencyResolver<Scope> parentDependencyResolver) {
        this.parentDependencyResolver = parentDependencyResolver;
        this.systemDependencyMap = new HashMap<>();
    }

    @Override
    public Optional<Dependency> resolve(Scope requestScope, String dependencyIdentifier) throws Exception {
        Optional<Dependency> foundDependency = resolve(dependencyIdentifier);
        if (!foundDependency.isPresent())
            if (parentDependencyResolver != null)
                return parentDependencyResolver.resolve(requestScope, dependencyIdentifier);
            else
                return Optional.empty();
        else
            return foundDependency;
    }

    public Optional<Dependency> resolve(String dependencyIdentifier) throws Exception {
        return Optional.ofNullable(systemDependencyMap.get(dependencyIdentifier));
    }

    public void add(String label, Dependency object) {
        set(label, object, false, false);
    }

    public void set(String label, Dependency object) {
        set(label, object, true, true);
    }

    public void setSilently(String label, Dependency object) {
        set(label, object, true, false);
    }

    private void set(String label, Dependency object, boolean replace, boolean reload) {
        if (!replace) {
            if (systemDependencyMap.containsKey(label))
                throw new IllegalStateException("Dependency with label " + label + " already exists.");
        }

        Dependency previousDependency = systemDependencyMap.put(label, object);
        if (previousDependency != null && reload) {
            Collection<DependencyConsumer> reevalSession = new HashSet<>();
            for (DependencyConsumer consumer : previousDependency.getDependents())
                consumer.reevaluate(reevalSession);
        }
    }

    public boolean has(String label) {
        return systemDependencyMap.containsKey(label);
    }

}
