package io.ibj.jsmc.core;

import io.ibj.jsmc.api.*;
import io.ibj.jsmc.api.exceptions.*;

import java.io.IOException;
import java.util.*;

/**
 * Simple dependency manager which takes a resolver to draw its modules from
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/13/16
 */
public class BasicDependencyManager<Scope> implements DependencyManager, DependencyConsumer {

   private static class DEntry implements DependencyManager.Entry {

       private final Dependency ref;
       private final DependencyLifecycle lifecycle;
       private final String identifier;

       private DEntry(Dependency ref, DependencyLifecycle lifecycle, String identifier) {
           this.ref = ref;
           this.lifecycle = lifecycle;
           this.identifier = identifier;
       }

       @Override
       public Dependency getReference() {
           return ref;
       }

       @Override
       public DependencyLifecycle getLifecycle() {
           return lifecycle;
       }

       @Override
       public String getIdentifier() {
           return identifier;
       }

       @Override
       public int hashCode() {
           return ref.hashCode();
       }

       @Override
       public boolean equals(Object obj) {
           if (obj instanceof DEntry)
               return ref.equals(((DEntry) obj).ref);
           else
               return false;
       }
   }

    private final DependencyResolver<Scope> dependencyResolver;
    private final Scope resolutionScope;
    private final Map<Dependency, DEntry> loadedModules;

    /**
     * Constructs a new dependency manager with resolver and scope
     * @param dependencyResolver internal dependency resolver
     * @param resolutionScope scope of module resolution
     */
    public BasicDependencyManager(DependencyResolver<Scope> dependencyResolver, Scope resolutionScope) {
        if (dependencyResolver == null)
            throw new NullPointerException("dependencyResolver cannot be null");

        this.dependencyResolver = dependencyResolver;
        this.resolutionScope = resolutionScope;
        this.loadedModules = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Dependency> getDependencies() {
        return loadedModules.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(String identifier) throws ModuleAlreadyLoadedException, ModuleCompilationException, ModuleExecutionException, ModuleNotFoundException, IOException {
        identifier = identifier.toLowerCase();
        Optional<Dependency> d = dependencyResolver.resolve(resolutionScope, identifier);
        if (d.isPresent()) {
            Dependency dependency = d.get();
            DEntry oldInfo = loadedModules.get(dependency);
            if (oldInfo != null)
                throw new ModuleAlreadyLoadedException(oldInfo, "Module previously loaded!");

            DependencyLifecycle lifecycle = dependency.depend(this);
            DEntry newInfo = new DEntry(dependency, lifecycle, identifier);
            loadedModules.put(dependency, newInfo);
        } else {
            throw new ModuleNotFoundException(identifier);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload(Entry moduleEntry) throws ModuleNotLoadedException, ModuleExecutionException {
        DEntry presentEntry = loadedModules.remove(moduleEntry.getReference());
        if (presentEntry == null)
            throw new ModuleNotLoadedException(moduleEntry.getIdentifier());
        try {
            presentEntry.getLifecycle().close();
        } catch (Exception e) {
            throw new ModuleExecutionException(e, "Exception occurred while closing module lifecycle");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Entry> getLoadedModules() {
        // java and it's generics FAIL AGAIN!!
        //noinspection unchecked
        return (Collection<Entry>) (Collection) loadedModules.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) throws ModuleExecutionException, IOException, ModuleCompilationException {
        List<DEntry> entries = new ArrayList<>(loadedModules.values());
        loadedModules.clear();
        for (DEntry i : entries) {
            try {
                load(i.getIdentifier());
            } catch (ModuleAlreadyLoadedException | ModuleNotFoundException e) {
                // todo - figure out the best way to handle this condition of exceptions which should rarely happen
                throw new RuntimeException(e);
            }
        }
    }
}
