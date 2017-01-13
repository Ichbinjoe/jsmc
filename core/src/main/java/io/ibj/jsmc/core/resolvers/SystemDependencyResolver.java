package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.api.exceptions.ModuleAlreadyLoadedException;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import io.ibj.jsmc.api.exceptions.ModuleNotFoundException;

import java.io.IOException;
import java.util.*;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
// todo - this needs exposed as an API for plugins to hook into to provide themselves as a dependency.
// todo - add javadocs
// todo - expose this in a public way in JsmcPlugin... maybe as a https://hub.spigotmc.org/javadocs/spigot/org/bukkit/plugin/RegisteredServiceProvider.html ?
public class SystemDependencyResolver<Scope> implements DependencyResolver<Scope> {

    private final Map<String, Dependency> systemDependencyMap;
    private final DependencyResolver<Scope> parentDependencyResolver;

    public SystemDependencyResolver(DependencyResolver<Scope> parentDependencyResolver) {
        this.parentDependencyResolver = parentDependencyResolver;
        this.systemDependencyMap = new HashMap<>();
    }

    @Override
    public Optional<Dependency> resolve(Scope requestScope, String dependencyIdentifier) throws ModuleCompilationException, IOException {
        Optional<Dependency> foundDependency = resolve(dependencyIdentifier);
        if (!foundDependency.isPresent())
            if (parentDependencyResolver != null)
                return parentDependencyResolver.resolve(requestScope, dependencyIdentifier);
            else
                return Optional.empty();
        else
            return foundDependency;
    }

    public Optional<Dependency> resolve(String dependencyIdentifier) throws ModuleCompilationException, IOException {
        // i am seriously surprised Map.get doesn't have a variant which spits out an optional. like really?!
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
        if (!replace && systemDependencyMap.containsKey(label))
            // todo - typed 'api' exception
            throw new IllegalStateException("Dependency with label " + label + " already exists.");

        Dependency previousDependency = systemDependencyMap.put(label, object);
        if (previousDependency != null && reload) {
            Collection<DependencyConsumer> reevalSession = new HashSet<>();
            for (DependencyConsumer consumer : previousDependency.getDependents())
                try {
                    consumer.reevaluate(reevalSession);
                } catch (ModuleExecutionException | IOException | ModuleAlreadyLoadedException | ModuleCompilationException | ModuleNotFoundException e) {
                    // todo - throw api exception
                }
        }
    }

    public boolean has(String label) {
        return systemDependencyMap.containsKey(label);
    }

}
