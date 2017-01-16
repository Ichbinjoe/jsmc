package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.api.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link JsonDependency}
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/15/17
 */
public class JsonDependencyTest extends DependencyContractTest {

    @Test
    public void testDependencyLifecycleGetExportReturnsConstructorObject() throws Exception {
        DependencyConsumer consumer = mock(DependencyConsumer.class);
        Object object = new Object();

        Dependency dependency = new JsonDependency(object);

        DependencyLifecycle lifecycle = dependency.depend(consumer);

        assertEquals(object, lifecycle.getDependencyExports());
    }

    @Override
    public Dependency createNewTestable() {
        return new JsonDependency(new Object());
    }
}