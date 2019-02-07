package io.ibj.jsmc.api;

import io.ibj.jsmc.api.exceptions.*;

import java.io.IOException;
import java.util.Collection;

/**
 * A singleton which manages dependencies within the jsmc ecosystem. Able to load and unload modules with no dependency
 * chain requirements. Mostly used for bootstrapping.
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public interface DependencyManager {

    /**
     * Represents an entry within the dependency manager
     */
    interface Entry {
        /**
         * Returns the dependency referenced by the entry
         *
         * @return dependency
         */
        Dependency getReference();

        /**
         * Returns the lifecycle belonging to the dependency manager. The .close() method should not be called except
         * by the manager. If you wish to unload the dependency, use {@link DependencyManager#unload(Entry)}
         *
         * @return dependency lifecycle related to the reference
         */
        DependencyLifecycle getLifecycle();

        /**
         * Returns the string based identifier of the module as loaded by the dependency manager
         *
         * @return dependency identifier, as was initially loaded
         */
        String getIdentifier();
    }

    /**
     * Loads a module, executes it, and holds the module in context of the DependencyManager
     *
     * @param moduleIdentifier String representation of a module identifier. Meaning specific to manager
     * @throws IllegalArgumentException     if moduleIdentifier is null or empty
     * @throws ModuleAlreadyLoadedException if moduleIdentifier has previously been loaded
     * @throws ModuleCompilationException   if module has issues compiling
     * @throws ModuleExecutionException     if module throws an exception
     * @throws ModuleNotFoundException      if module doesn't actually exist
     * @throws IOException                  if an io related exception occurs
     * @return Reference to module which was loaded
     */
    Entry load(String moduleIdentifier) throws ModuleAlreadyLoadedException, ModuleCompilationException, ModuleExecutionException, ModuleNotFoundException, IOException;

    /**
     * Unloads a module from the dependency manager.
     *
     * @param moduleEntry Module entry reference to be unloaded
     * @throws IllegalArgumentException if moduleEntry is null
     * @throws ModuleNotLoadedException if module entry passed does not belong to this dependency manager
     * @throws ModuleExecutionException if module throws an exception
     */
    void unload(Entry moduleEntry) throws ModuleNotLoadedException, ModuleExecutionException;

    /**
     * Returns the internal collection of modules which are currently loaded
     * @return Collection of currently loaded modules
     */
    Collection<Entry> getLoadedModules();

}
