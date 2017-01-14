package io.ibj.jsmc.api;

import io.ibj.jsmc.api.exceptions.ModuleAlreadyLoadedException;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import io.ibj.jsmc.api.exceptions.ModuleNotFoundException;

import java.io.IOException;
import java.util.Collection;

// todo - tests

/**
 * Able to consume {@link Dependency}s. Also able to reevaluate all of the dependencies in which it holds
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/2/16
 */
public interface DependencyConsumer {

    /**
     * Returns a collection of all dependencies to which this consumer holds
     *
     * @return Collection of necessary dependencies
     */
    Collection<Dependency> getDependencies();

    /**
     * Reevaluates the consumer's dependence on its dependencies, usually triggered by an upstream dependency change. If
     * any consumers depend on this module, then those modules must be triggered to reevaluate as well.
     * <p>
     * At the beginning of a reevaluate sequence, a clean collection must be passed in to begin evaluation. As each
     * consumer reevaluates, it must add itself to the previouslyEvaluatedConsumers. If triggering more consumers,
     * implementations must not trigger ones which are already present in 'previouslyEvaluatedConsumers'
     *
     * @param previouslyEvaluatedConsumers Collection of consumers which have already been evaluated, and should not be
     *                                     reevaluated
     * @throws ModuleExecutionException   if a module fails to execute properly
     * @throws IOException                if an IO related exception is thrown
     * @throws ModuleCompilationException if a module is recompiled and throws an exception
     * @throws ModuleNotFoundException    if during reevaluation, a module dependency is lost
     */
    void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) throws ModuleExecutionException, IOException, ModuleCompilationException, ModuleNotFoundException;

}
