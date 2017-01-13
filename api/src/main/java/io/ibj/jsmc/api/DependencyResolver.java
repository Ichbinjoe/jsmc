package io.ibj.jsmc.api;

import io.ibj.jsmc.api.exceptions.ModuleCompilationException;

import java.io.IOException;
import java.util.Optional;

// todo - tests

/**
 * A method of resolving dependencies given context to a certain scope. Scope may be anything, but commonly refer to a
 * path on the filesystem.
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 8/29/16
 */
public interface DependencyResolver<Scope> {

    /**
     * Attempts to resolve a dependency given a scope of request and a identifying file.
     *
     * @param requestScope         Scope of the dependency resolution
     * @param dependencyIdentifier identifier of the dependency which needs resolved
     * @return Optional of the resolved dependency. Will be empty if the dependency was not found
     * @throws ModuleCompilationException if the module fails to be loaded / compiled correctly
     * @throws IOException in the event of an IO based exception
     */
    Optional<Dependency> resolve(Scope requestScope, String dependencyIdentifier) throws ModuleCompilationException, IOException;

}
