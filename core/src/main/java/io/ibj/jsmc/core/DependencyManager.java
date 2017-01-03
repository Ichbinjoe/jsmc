package io.ibj.jsmc.core;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyLifecycle;
import io.ibj.jsmc.api.DependencyResolver;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/13/16
 */
public class DependencyManager<Scope> implements DependencyConsumer {

    private final DependencyResolver<Scope> dependencyResolver;
    private final Scope resolutionScope;
    private final Map<Dependency, DependencyInformation> dependencyMap;
    private final Set<String> previouslyLoadedModules;

    public DependencyManager(DependencyResolver<Scope> dependencyResolver, Scope resolutionScope) {
        this.dependencyResolver = dependencyResolver;
        this.resolutionScope = resolutionScope;
        this.dependencyMap = new HashMap<>();
        this.previouslyLoadedModules = new HashSet<>();
    }


    @Override
    public Collection<Dependency> getDependencies() {
        return dependencyMap.keySet();
    }

    public void load(String identifier) throws Exception {
        identifier = identifier.toLowerCase();
        previouslyLoadedModules.remove(identifier); // remove the module as possible, in the case it was deleted from the fs or what not
        Optional<Dependency> d = dependencyResolver.resolve(resolutionScope, identifier);
        if (d.isPresent()) {
            Dependency dependency = d.get();
            DependencyInformation oldInfo = dependencyMap.get(dependency);
            if (oldInfo != null)
                throw new IllegalStateException("Dependency '" + identifier + "' was already loaded!");

            DependencyLifecycle lifecycle = dependency.depend(this);
            DependencyInformation newInfo = new DependencyInformation(identifier, lifecycle);
            dependencyMap.put(dependency, newInfo);
        } else {
            throw new IllegalArgumentException("Dependency '" + identifier + "' does not exist!");
        }
    }

    public void unload(String identifier) {
        identifier = identifier.toLowerCase();
        Iterator<Map.Entry<Dependency, DependencyInformation>> iter = dependencyMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Dependency, DependencyInformation> entry = iter.next();
            if (entry.getValue().getDependencyCallName().equalsIgnoreCase("identifier")) {
                iter.remove();
                previouslyLoadedModules.add(entry.getValue().getDependencyCallName());
                try {
                    entry.getValue().getLifecyle().close();
                } catch (Exception e) {
                    throw new IllegalStateException("Exception occured while trying to unload '" + identifier + "'!", e);
                }
                return;
            }
        }
        throw new IllegalStateException("No dependency with identifier '" + identifier + "' exists!");
    }

    public void unloadAll() {
        for (DependencyInformation info : dependencyMap.values()) {
            try {
                info.getLifecyle().close();
            } catch (Exception e) {
                System.err.println("Exception occurred while trying to unload '" + info.getDependencyCallName() + "':");
                e.printStackTrace();
            }
        }
    }

    public Collection<String> getLoadedModules() {
        return dependencyMap.values().stream().map(DependencyInformation::getDependencyCallName).collect(Collectors.toSet());
    }

    public Collection<String> getUnloadedModules() {
        return previouslyLoadedModules;
    }

    @Override
    public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) {
        List<DependencyInformation> infoCopy = new ArrayList<>(dependencyMap.values());
        for (DependencyInformation i : infoCopy) {
            try {
                load(i.getDependencyCallName());
            } catch (Exception e) {
                System.err.println("Exception caught while trying to reevaluate dependencies for dependency with identifier '" + i.getDependencyCallName() + "':");
                e.printStackTrace();
            }
        }
    }

    public static final class DependencyInformation {
        private final String dependencyCallName;
        private final DependencyLifecycle lifecyle;

        public DependencyInformation(String dependencyCallName, DependencyLifecycle lifecyle) {
            this.dependencyCallName = dependencyCallName;
            this.lifecyle = lifecyle;
        }

        public String getDependencyCallName() {
            return dependencyCallName;
        }

        public DependencyLifecycle getLifecyle() {
            return lifecyle;
        }
    }
}
