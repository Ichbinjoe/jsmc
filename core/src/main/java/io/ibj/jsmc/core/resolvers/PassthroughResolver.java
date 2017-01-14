package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.api.exceptions.ModuleCompilationException;

import java.io.IOException;
import java.util.Optional;

/**
 * Attempts a fileSystem resolution, then diverts the resolution to the downstreamResolver. Good for glueing different
 * resolvers together
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/12/16
 */
public class PassthroughResolver<Scope> implements DependencyResolver<Scope> {

    private final DependencyResolver<Scope> fileSystemResolver;
    private final DependencyResolver<? super Scope> downstreamResolver;

    /**
     * Creates a new passthrough resolver
     * @param fileSystemResolver initial resolver to call
     * @param downstreamResolver secondary resolver to call
     */
    public PassthroughResolver(DependencyResolver<Scope> fileSystemResolver, DependencyResolver<? super Scope> downstreamResolver) {
        if (fileSystemResolver == null)
            throw new NullPointerException("fileSystemResolver cannot be null");
        if (downstreamResolver == null)
            throw new NullPointerException("downstreamResolver cannot be null");

        this.fileSystemResolver = fileSystemResolver;
        this.downstreamResolver = downstreamResolver;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public Optional<Dependency> resolve(Scope requestScope, String dependencyIdentifier) throws ModuleCompilationException, IOException {
        Optional<Dependency> ret = fileSystemResolver.resolve(requestScope, dependencyIdentifier);
        if (!ret.isPresent())
            return downstreamResolver.resolve(requestScope, dependencyIdentifier);
        return ret;
    }
}
