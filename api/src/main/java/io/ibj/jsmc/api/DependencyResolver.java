package io.ibj.jsmc.api;

import java.util.Optional;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 8/29/16
 */
public interface DependencyResolver<Scope> {

    Optional<Dependency> resolve(Scope requestScope, String dependencyIdentifier) throws Exception;

}
