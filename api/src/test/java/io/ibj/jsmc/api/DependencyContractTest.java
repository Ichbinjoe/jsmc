package io.ibj.jsmc.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link Dependency} implementations against the contract defined in documentation
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/15/17
 */
public abstract class DependencyContractTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public abstract Dependency createNewTestable() throws Exception;

    @Test
    public void testNullDependencyConsumerThrowsNullPointer() throws Exception {
        Dependency dependency = createNewTestable();

        exception.expect(NullPointerException.class);

        dependency.depend(null);
    }

    @Test
    public void testDoubleDependReturnsSameLifecycle() throws Exception {
        DependencyConsumer dependencyConsumer = mock(DependencyConsumer.class);

        Dependency dependency = createNewTestable();

        DependencyLifecycle lifecycle = dependency.depend(dependencyConsumer);

        DependencyLifecycle lifecycle2 = dependency.depend(dependencyConsumer);

        assertEquals(lifecycle, lifecycle2);
    }

    @Test
    public void testGetDependentsContainsConsumerWhenDepending() throws Exception {
        DependencyConsumer dependencyConsumer = mock(DependencyConsumer.class);

        Dependency dependency = createNewTestable();

        assertEquals(0, dependency.getDependents().size());

        DependencyLifecycle lifecycle = dependency.depend(dependencyConsumer);

        // contains only dependencyConsumer
        Collection<DependencyConsumer> dependents = dependency.getDependents();
        assertEquals(1, dependents.size());
        assertTrue(dependents.contains(dependencyConsumer));

        lifecycle.close();

        // no longer contains dependencyConsumer as a dependent
        assertEquals(0, dependency.getDependents().size());
    }

}
