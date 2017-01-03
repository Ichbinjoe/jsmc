package io.ibj.jsmc.api;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/2/16
 */
public interface DependencyLifecycle extends AutoCloseable {

   Object getDependencyExports();

   Dependency getParentDependency();

}
