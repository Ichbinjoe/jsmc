package io.ibj.jsmc.api;

import io.ibj.jsmc.api.exceptions.ModuleAlreadyLoadedException;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import io.ibj.jsmc.api.exceptions.ModuleNotLoadedException;

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
     * @throws ModuleExecutionException     if module throws an exception
     */
    void load(String moduleIdentifier) throws ModuleAlreadyLoadedException, ModuleExecutionException;

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
     * Unloads all modules from the dependency manager.
     *
     * @throws ModuleExecutionException if any of the modules fail to unload without exception. If an exception occurs,
     *                                  the DependencyManager will still make best effort to continue to unload all
     *                                  modules. At the end, the exception will be thrown with a concatenation of all
     *                                  exceptions incurred during unload
     */
    void unloadAll() throws ModuleExecutionException;

    /**
     * Returns the internal collection of modules which are currently loaded
     * @return Collection of currently loaded modules
     */
    Collection<Entry> getLoadedModules();

    /**
     * Returns a collection of strings of modules which is able to be loaded. Does not include already loaded modules
     * @return Collection of not loaded but loadable modules
     */
    Collection<String> getLoadableModules();

}
