package io.ibj.jsmc.api.exceptions;

/**
 * Thrown when a module fails to execute a section of triggered code correctly
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class ModuleExecutionException extends Exception {

    /**
     * Creates a ModuleExecutionException with an associated source exception and message
     *
     * @param t       source exception
     * @param message message
     */
    public ModuleExecutionException(Throwable t, String message) {
        super(message, t);
    }
}
