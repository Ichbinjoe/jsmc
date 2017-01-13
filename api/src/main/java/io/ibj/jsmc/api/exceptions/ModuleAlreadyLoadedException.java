package io.ibj.jsmc.api.exceptions;

import io.ibj.jsmc.api.DependencyManager;

/**
 * Thrown when an attempt to load a module is made, but the module was already loaded.
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class ModuleAlreadyLoadedException extends Exception {

    private final DependencyManager.Entry loadedEntry;

    /**
     * Creates a new ModuleAlreadyLoadedException with the given loaded entry and message
     * @param entry
     * @param message
     */
    public ModuleAlreadyLoadedException(DependencyManager.Entry entry, String message) {
        super(message);
        this.loadedEntry = entry;
    }

    /**
     * Returns the entry which was previously loaded
     * @return entry
     */
    public DependencyManager.Entry getLoadedEntry() {
        return loadedEntry;
    }
}
