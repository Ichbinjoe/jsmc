package io.ibj.jsmc.api.exceptions;

/**
 * Thrown when a module fails to be compiled correctly
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class ModuleCompilationException extends Exception {
    /**
     * Creates a compilation exception with an associated source exception
     *
     * @param e       Source exception
     * @param message message associated with exception
     */
    public ModuleCompilationException(Exception e, String message) {
        super(message, e);
    }
}
