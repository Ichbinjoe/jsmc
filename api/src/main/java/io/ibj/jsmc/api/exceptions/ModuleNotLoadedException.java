package io.ibj.jsmc.api.exceptions;

/**
 * Thrown when a request to unload a module is executed, but there is no module to unload
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class ModuleNotLoadedException extends Exception {

    private final String moduleIdentifier;

    /**
     * Creates a new ModuleNotLoadedException with a module identifier
     *
     * @param moduleIdentifier module which was not loaded
     */
    public ModuleNotLoadedException(String moduleIdentifier) {
        this.moduleIdentifier = moduleIdentifier;
    }

    /**
     * Returns the identifier of the module which was not loaded
     * @return unloaded module identifier
     */
    public String getModuleIdentifier() {
        return moduleIdentifier;
    }
}
