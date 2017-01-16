package io.ibj.jsmc.core.resolvers;

import io.ibj.jsmc.api.Dependency;
import io.ibj.jsmc.api.DependencyConsumer;
import io.ibj.jsmc.api.DependencyResolver;
import io.ibj.jsmc.api.SystemDependency;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/16/17
 */
public class SystemDependencyResolverTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNonexistantDependencyNullParentReturnsEmpty() throws Exception {
        SystemDependencyResolver resolver = new SystemDependencyResolver(null);
        Optional<Dependency> dependency = resolver.resolve(null, "not-here");
        assertFalse(dependency.isPresent());
    }

    @Test
    public void testNonexistantDependencyParentExistsReturnsParentDependency() throws Exception {
        Dependency parentDependency = mock(Dependency.class);
        DependencyResolver parentResolver = mock(DependencyResolver.class);
        when(parentResolver.resolve(null, "dependency")).thenReturn(Optional.of(parentDependency));

        SystemDependencyResolver resolver = new SystemDependencyResolver(parentResolver);
        Optional<Dependency> dependency = resolver.resolve(null, "dependency");
        assertTrue(dependency.isPresent());
        assertEquals(parentDependency, dependency.get());
    }

    @Test
    public void testNonExistantDependencyParentNonExistantReturnsEmpty() throws Exception {
        DependencyResolver parentResolver = mock(DependencyResolver.class);
        when(parentResolver.resolve(null, "dependency")).thenReturn(Optional.empty());

        SystemDependencyResolver resolver = new SystemDependencyResolver(parentResolver);
        Optional<Dependency> dependency = resolver.resolve(null, "dependency");

        assertFalse(dependency.isPresent());
    }

    @Test
    public void testAddDependencyNoDuplicates() throws Exception {
        Dependency dependency = mock(Dependency.class);

        SystemDependencyResolver resolver = new SystemDependencyResolver(null);

        resolver.add("dependency", dependency);

        Optional<Dependency> resolveResult = resolver.resolve(null, "dependency");

        assertTrue(resolveResult.isPresent());
        assertEquals(dependency, resolveResult.get());
    }

    @Test
    public void testAddDependencyDuplicateThrowsIllegalStateException() throws Exception {
        Dependency dependency = mock(Dependency.class);
        SystemDependencyResolver resolver = new SystemDependencyResolver(null);

        resolver.add("dependency", dependency);

        Dependency dependency2 = mock(Dependency.class);

        exception.expect(IllegalStateException.class);
        resolver.add("dependency", dependency2);
    }

    @Test
    public void testAddDependencyBadNameThrowsIllegalArgumentException() throws Exception {
        Dependency dependency = mock(Dependency.class);
        SystemDependencyResolver resolver = new SystemDependencyResolver(null);

        exception.expect(IllegalArgumentException.class);
        resolver.add("dependency!", dependency);
    }

    @Test
    public void testSetDependencyOverwriteOld() throws Exception {
        Dependency dependency = mock(Dependency.class);
        SystemDependencyResolver resolver = new SystemDependencyResolver(null);

        resolver.add("dependency", dependency);

        Optional<Dependency> resolveOne = resolver.resolve(null, "dependency");
        assertTrue(resolveOne.isPresent());
        assertEquals(dependency, resolveOne.get());

        Dependency dependency2 = mock(Dependency.class);

        resolver.set("dependency", dependency2);
        Optional<Dependency> resolveTwo = resolver.resolve(null, "dependency");
        assertTrue(resolveTwo.isPresent());
        assertEquals(dependency2, resolveTwo.get());
    }

    @Test
    public void testDoesNotHaveReturnsFalse() throws Exception {
        SystemDependencyResolver resolver = new SystemDependencyResolver(null);
        assertFalse(resolver.has("dependency"));
    }

    @Test
    public void testHasReturnsTrue() throws Exception {
        Dependency dependency = mock(Dependency.class);

        SystemDependencyResolver resolver = new SystemDependencyResolver(null);
        resolver.add("dependency", dependency);

        assertTrue(resolver.has("dependency"));
    }

}