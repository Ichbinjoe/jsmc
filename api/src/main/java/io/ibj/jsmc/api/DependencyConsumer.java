package io.ibj.jsmc.api;

import java.util.Collection;

// todo - javadocs

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/2/16
 */
public interface DependencyConsumer {

    Collection<Dependency> getDependencies();

    void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers);

}
