package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyResolver;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/16/17
 */
public class PassthroughResolverTest {

    @Test
    public void testResolveFirstHitReturnsFirst() throws Exception {
        Object scope = new Object();

        DependencyResolver firstResolver = mock(DependencyResolver.class);
        DependencyResolver secondResolver = mock(DependencyResolver.class);

        Dependency responseOne = mock(Dependency.class);
        Dependency responseTwo = mock(Dependency.class);

        when(firstResolver.resolve(scope, "dependency")).thenReturn(Optional.of(responseOne));
        when(secondResolver.resolve(scope, "dependency")).thenReturn(Optional.of(responseTwo));

        PassthroughResolver resolver = new PassthroughResolver(firstResolver, secondResolver);

        Optional<Dependency> resolveResult = resolver.resolve(scope, "dependency");

        assertTrue(resolveResult.isPresent());
        assertEquals(responseOne, resolveResult.get());

        verify(firstResolver, atLeastOnce()).resolve(scope, "dependency");
        verify(secondResolver, never()).resolve(scope, "dependency");
    }

    @Test
    public void testResolveFirstMissSecondHitReturnsSecond() throws Exception {
        Object scope = new Object();

        DependencyResolver firstResolver = mock(DependencyResolver.class);
        DependencyResolver secondResolver = mock(DependencyResolver.class);

        Dependency responseTwo = mock(Dependency.class);

        when(firstResolver.resolve(scope, "dependency")).thenReturn(Optional.empty());
        when(secondResolver.resolve(scope, "dependency")).thenReturn(Optional.of(responseTwo));

        PassthroughResolver resolver = new PassthroughResolver(firstResolver, secondResolver);

        Optional<Dependency> resolveResult = resolver.resolve(scope, "dependency");

        assertTrue(resolveResult.isPresent());
        assertEquals(responseTwo, resolveResult.get());

        verify(firstResolver, atLeastOnce()).resolve(scope, "dependency");
        verify(secondResolver, atLeastOnce()).resolve(scope, "dependency");
    }

    @Test
    public void testResolveBothMissReturnsEmpty() throws Exception {
        Object scope = new Object();

        DependencyResolver firstResolver = mock(DependencyResolver.class);
        DependencyResolver secondResolver = mock(DependencyResolver.class);

        when(firstResolver.resolve(scope, "dependency")).thenReturn(Optional.empty());
        when(secondResolver.resolve(scope, "dependency")).thenReturn(Optional.empty());

        PassthroughResolver resolver = new PassthroughResolver(firstResolver, secondResolver);

        Optional<Dependency> resolveResult = resolver.resolve(scope, "dependency");

        assertFalse(resolveResult.isPresent());

        verify(firstResolver, atLeastOnce()).resolve(scope, "dependency");
        verify(secondResolver, atLeastOnce()).resolve(scope, "dependency");
    }
}