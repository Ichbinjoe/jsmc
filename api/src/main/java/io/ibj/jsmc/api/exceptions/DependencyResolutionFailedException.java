package io.ibj.jsmc.api.exceptions;

/**
 * Thrown when a script requires a module, but the module doesn't actually exist (for whatever reason), or if in the
 * process of requiring the module, an exception was thrown
 *
 * Runtime exception due to the wrapping that must be done.
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/13/17
 */
public class DependencyResolutionFailedException extends RuntimeException {

    private final String identifier;

    /**
     * Creates a resolution failed exception where the identifier simply wasn't found. No additional stack traces are
     * kept if this constructor is used
     *
     * @param identifier identifier which could not be found
     */
    public DependencyResolutionFailedException(String identifier) {
        super("No dependency is known by the identifier passed (" + identifier + ")");
        this.identifier = identifier;
    }

    /**
     * Creates a resolution failed exception where another exception was thrown during the resolution mechanism.
     *
     * @param identifier identifier which caused the underlying exception
     * @param t          throwable which was thrown
     */
    public DependencyResolutionFailedException(String identifier, Throwable t) {
        super("An exception was thrown while attempting to resolve a dependency", t);
        this.identifier = identifier;
    }

    /**
     * Returns the identifier which caused the exception
     *
     * @return cause identifier
     */
    public String getIdentifier() {
        return identifier;
    }
}
