package io.ibj.jsmc.api;

import io.ibj.jsmc.api.exceptions.ModuleExecutionException;

import java.util.Collection;

/**
 * A dependency is something which can be depended on and can offer {@link DependencyLifecycle} of itself, holding
 * internal state information.
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 8/29/16
 */
public interface Dependency {

    /**
     * Starts a dependency relationship with the passed dependency consumer. Returns a lifecycle from which internal
     * state can be retrieved from, and the relationship closed from.
     *
     * If a consumer asks for multiple lifecycles, the same should be returned.
     *
     * @param dependencyConsumer Consumer to which the lifecycle should be tied to
     * @return Lifecycle which exposes the internal state as well as a close method
     * @throws ModuleExecutionException if depending on the object causes execution, and thus causes another exception
     * @throws NullPointerException if dependencyConsumer is null
     */
    DependencyLifecycle depend(DependencyConsumer dependencyConsumer) throws ModuleExecutionException;

    /**
     * Collection of all dependents of this dependency
     * @return collection of dependents
     */
    Collection<DependencyConsumer> getDependents();

}
