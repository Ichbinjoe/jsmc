package io.ibj.jsmc.api;

// todo - tests

/**
 * Lifecycle relationship between a {@link Dependency} and a {@link DependencyConsumer}. Exposes the internal state of a
 * dependency, and allows for closure of the link through an {@link AutoCloseable} interface
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/2/16
 */
public interface DependencyLifecycle extends AutoCloseable {

    /**
     * Returns the internal state of the dependency, commonly referred to as the dependency's 'exports'. This value may
     * be null if the dependency has nothing to export in this lifecycle
     * @return exports
     * @throws IllegalStateException if the lifecycle was already closed
     */
    Object getDependencyExports();

    /**
     * Parent dependency of this lifecycle. Since the lifecycle must be stored by the {@link DependencyConsumer}, this
     * method is an easy reference to link it back.
     * @return parent dependency
     */
    Dependency getParentDependency();

}
