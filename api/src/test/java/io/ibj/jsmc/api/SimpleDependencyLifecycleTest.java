package io.ibj.jsmc.api;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link SimpleDependencyLifecycle}
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class SimpleDependencyLifecycleTest extends DependencyLifecycleContractTest {

    @Test
    public void testConstructorNullParentThrowsNullPointerException() {
        exception.expect(NullPointerException.class);
        new SimpleDependencyLifecycle(null, new Object(), () -> {
        });
    }

    @Test
    public void testCallbackCalledWhenLifecycleIsClosed() throws Exception {
        Dependency dependency = mock(Dependency.class);
        Object object = new Object();

        final AtomicBoolean callbackHit = new AtomicBoolean(false);

        Runnable callBack = () -> {
            boolean callbackHitPreviously = callbackHit.getAndSet(true);
            if (callbackHitPreviously)
                throw new IllegalStateException("Callback called twice");
        };

        SimpleDependencyLifecycle lifecycle = new SimpleDependencyLifecycle(dependency, object, callBack);

        lifecycle.close();

        assertTrue(callbackHit.get());
    }

    @Test
    public void testCallbackOnlyCalledOnceWhenLifecycleDoubleClosed() throws Exception {
        Dependency dependency = mock(Dependency.class);
        Object object = new Object();

        final AtomicBoolean callbackHit = new AtomicBoolean(false);

        Runnable callBack = () -> {
            boolean callbackHitPreviously = callbackHit.getAndSet(true);
            if (callbackHitPreviously)
                throw new IllegalStateException("Callback called twice");
        };

        SimpleDependencyLifecycle lifecycle = new SimpleDependencyLifecycle(dependency, object, callBack);

        lifecycle.close();

        callbackHit.set(false);

        try {
            lifecycle.close();
        } catch (IllegalStateException e) {
            // A true callbackHit would mean that the callback was called again, even though the close was an illegal invocation
            assertFalse(callbackHit.get());
            return;
        }
        // the test should never get here. the second #close should always result in an IllegalStateException
        fail();
    }

    @Override
    Resources createNewTestable(DependencyConsumer consumer) {
        Dependency dependency = mock(Dependency.class);
        Object internalObject = new Object();
        SimpleDependencyLifecycle lifecycle = new SimpleDependencyLifecycle(dependency, internalObject, () -> {
        });

        return new Resources(dependency, lifecycle, internalObject);
    }
}