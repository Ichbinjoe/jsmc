package io.ibj.jsmc.api;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link DependencyLifecycle} implementations against the contract defined in documentation
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/15/17
 */
public abstract class DependencyLifecycleContractTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    abstract Resources createNewTestable(DependencyConsumer consumer);

    @Test
    public void testGetDependencyExportsReturnsInternalObject() throws Exception {
        DependencyConsumer consumer = mock(DependencyConsumer.class);

        Resources testResources = createNewTestable(consumer);
        Object internalObject = testResources.internalObject;

        DependencyLifecycle lifecycle = testResources.lifecycle;

        assertEquals(internalObject, lifecycle.getDependencyExports());
    }

    @Test
    public void testGetDependencyExportsThrowsIllegalStateExceptionOnAccessAfterClose() throws Exception {

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = createNewTestable(consumer).lifecycle;

        lifecycle.close();

        exception.expect(IllegalStateException.class);
        lifecycle.getDependencyExports();
    }

    @Test
    public void testGetDependencyExportsReturnsSameObjectMultipleInvocations() throws Exception {
        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = createNewTestable(consumer).lifecycle;

        Object firstInvoke = lifecycle.getDependencyExports();
        Object secondInvoke = lifecycle.getDependencyExports();

        assertEquals(firstInvoke, secondInvoke);
    }

    @Test
    public void testParentDependencyIsActuallyParentDependency() throws Exception {
        DependencyConsumer consumer = mock(DependencyConsumer.class);

        Resources testResources = createNewTestable(consumer);

        assertEquals(testResources.parentDependency, testResources.lifecycle.getParentDependency());
    }

    @Test
    public void testParentDependencyIsAccessibleAfterClose() throws Exception {
        DependencyConsumer consumer = mock(DependencyConsumer.class);

        Resources testResources = createNewTestable(consumer);

        testResources.lifecycle.close();

        assertEquals(testResources.parentDependency, testResources.lifecycle.getParentDependency());
    }

    @Test
    public void testDoubleCloseThrowsIllegalStateException() throws Exception {
        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = createNewTestable(consumer).lifecycle;

        lifecycle.close();

        exception.expect(IllegalStateException.class);

        lifecycle.close();
    }

    @AllArgsConstructor
    @Value
    public static class Resources {
        Dependency parentDependency;
        DependencyLifecycle lifecycle;
        Object internalObject;
    }

}
