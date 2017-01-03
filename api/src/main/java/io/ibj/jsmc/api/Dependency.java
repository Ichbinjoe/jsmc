package io.ibj.jsmc.api;

import java.util.Collection;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 8/29/16
 */
public interface Dependency {

    DependencyLifecycle depend(DependencyConsumer dependencyConsumer);

    Collection<DependencyConsumer> getDependents();

}
