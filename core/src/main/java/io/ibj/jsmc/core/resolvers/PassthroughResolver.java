package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyResolver;

import java.util.Optional;

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/12/16
 */
public class PassthroughResolver<Scope> implements DependencyResolver<Scope> {

    private final DependencyResolver<Scope> fileSystemResolver;
    private final DependencyResolver<? super Scope> downstreamResolver;

    public PassthroughResolver(DependencyResolver<Scope> fileSystemResolver, DependencyResolver<? super Scope> downstreamResolver) {
        this.fileSystemResolver = fileSystemResolver;
        this.downstreamResolver = downstreamResolver;
    }

    @Override
    public Optional<Dependency> resolve(Scope requestScope, String dependencyIdentifier) throws Exception {
        Optional<Dependency> ret = fileSystemResolver.resolve(requestScope, dependencyIdentifier);
        if (!ret.isPresent())
            return downstreamResolver.resolve(requestScope, dependencyIdentifier);
        return ret;
    }
}
