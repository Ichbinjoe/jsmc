package io.ibj.jsmc.api.exceptions;

/**
 * Thrown when a request for a module is made, and at the API layer must report that the module was not found.
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/13/17
 */
public class ModuleNotFoundException extends Exception {

    private final String identifierNotFound;

    /**
     * Constructs a new ModuleNotFoundException with the identifier
     * @param identifierNotFound identifier queried which was not found
     */
    public ModuleNotFoundException(String identifierNotFound) {
        this.identifierNotFound = identifierNotFound;
    }

    @Override
    public String getMessage() {
        return "Module with identifier '" + identifierNotFound + "' could not be found";
    }

    /**
     * Returns the unfound module identifier
     * @return unfound identifier
     */
    public String getUnfoundIdentifier() {
        return identifierNotFound;
    }

}
