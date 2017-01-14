package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.api.SystemDependencyHook;
import io.ibj.jsmc.api.exceptions.ModuleAlreadyLoadedException;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import io.ibj.jsmc.api.exceptions.ModuleNotFoundException;

import java.io.IOException;
import java.util.*;

/**
 * Internal dependency manager which is able to also act as a dependency resolver
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
// todo - expose this in a public way in JsmcPlugin... maybe as a https://hub.spigotmc.org/javadocs/spigot/org/bukkit/plugin/RegisteredServiceProvider.html ?
public class SystemDependencyResolver<Scope> implements SystemDependencyHook, DependencyResolver<Scope> {

    private final Map<String, Dependency> systemDependencyMap;
    private final DependencyResolver<Scope> parentDependencyResolver;

    /**
     * Creates an system dependency resolver with a fallback resolver
     * @param parentDependencyResolver fallback resolver
     */
    public SystemDependencyResolver(DependencyResolver<Scope> parentDependencyResolver) {
        this.parentDependencyResolver = parentDependencyResolver;
        this.systemDependencyMap = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public Optional<Dependency> resolve(String dependencyIdentifier) throws ModuleCompilationException, IOException {
        // i am seriously surprised Map.get doesn't have a variant which spits out an optional. like really?!
        return Optional.ofNullable(systemDependencyMap.get(dependencyIdentifier));
    }

    /**
     * {@inheritDoc}
     */
    public void add(String label, Dependency object) {
        set(label, object, false, false);
    }

    /**
     * {@inheritDoc}
     */
    public void set(String label, Dependency object) {
        set(label, object, true, true);
    }

    /**
     * {@inheritDoc}
     */
    public void setSilently(String label, Dependency object) {
        set(label, object, true, false);
    }

    private void set(String label, Dependency object, boolean replace, boolean reload) {
        if (object == null)
            throw new NullPointerException("object may not be null");

        if (!ModuleResolver.validModulePattern.matcher(label).matches())
            throw new IllegalArgumentException("label does not follow module naming conventions");

        if (!replace && systemDependencyMap.containsKey(label))
            throw new IllegalStateException("Dependency with label " + label + " already exists");

        Dependency previousDependency = systemDependencyMap.put(label, object);
        if (previousDependency != null && reload) {
            Collection<DependencyConsumer> reevalSession = new HashSet<>();
            for (DependencyConsumer consumer : previousDependency.getDependents()) {
                try {
                    consumer.reevaluate(reevalSession);
                } catch (ModuleExecutionException | IOException | ModuleAlreadyLoadedException |
                        ModuleCompilationException | ModuleNotFoundException e) {
                    throw new RuntimeException("On a module reevaluate, a module was unable to load and reevaluate", e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean has(String label) {
        return systemDependencyMap.containsKey(label);
    }

}
