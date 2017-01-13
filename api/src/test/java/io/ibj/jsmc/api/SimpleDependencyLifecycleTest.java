package io.ibj.jsmc.api;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests {@link SimpleDependencyLifecycle}
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class SimpleDependencyLifecycleTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void constructorAllNullParent() {
        exception.expect(NullPointerException.class);
        new SimpleDependencyLifecycle(null, new Object(), () -> {
        });
    }

    @Test
    public void doubleClose() throws Exception {
        Object o = new Object();
        Dependency d = new SystemDependency(o);
        SimpleDependencyLifecycle l = new SimpleDependencyLifecycle(d, o);
        l.close();

        exception.expect(IllegalStateException.class);
        l.close();
    }

    @Test
    public void accessAfterClose() throws Exception {
        Object o = new Object();
        Dependency d = new SystemDependency(o);
        SimpleDependencyLifecycle l = new SimpleDependencyLifecycle(d, o);

        l.close();

        exception.expect(IllegalStateException.class);
        l.getDependencyExports();
    }

    @Test
    public void sameExportsAsPassed() throws Exception {
        Object o = new Object();
        Dependency d = new SystemDependency(o);
        SimpleDependencyLifecycle l = new SimpleDependencyLifecycle(d, o);

        Assert.assertEquals(o, l.getDependencyExports());
        Assert.assertEquals(d, l.getParentDependency());
    }

    @Test
    public void callbackCalled() throws Exception {
        Object o = new Object();
        Dependency d = new SystemDependency(o);
        boolean[] callbackHit = new boolean[1];
        callbackHit[0] = false;
        Runnable r = () -> {
            if (callbackHit[0]) throw new IllegalStateException("Callback called twice");
            callbackHit[0] = true;
        };
        SimpleDependencyLifecycle l = new SimpleDependencyLifecycle(d, o, r);

        l.close();

        Assert.assertTrue(callbackHit[0]);
        callbackHit[0] = false;
        try {
            l.close();
        } catch (IllegalStateException e) {
            Assert.assertFalse(callbackHit[0]);
            return;
        }
        // no exception
        Assert.fail();
    }
}