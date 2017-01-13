package io.ibj.jsmc.api;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Collections;

/**
 * Tests {@link SystemDependency}
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/12/17
 */
public class SystemDependencyTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public DependencyConsumer createEmptyConsumer() {
        return new DependencyConsumer() {
            @Override
            public Collection<Dependency> getDependencies() {
                return Collections.EMPTY_SET;
            }

            @Override
            public void reevaluate(Collection<DependencyConsumer> previouslyEvaluatedConsumers) {
            }
        };
    }

    @Test
    public void dependencyPassthrough() {
        Object o = new Object();
        Dependency d = new SystemDependency(o);
        DependencyConsumer c = createEmptyConsumer();
        DependencyLifecycle l = d.depend(c);
        Assert.assertEquals(o, l.getDependencyExports());
        Assert.assertEquals(d, l.getParentDependency());
        Assert.assertTrue(d.getDependents().size() == 1);
        Assert.assertTrue(d.getDependents().contains(c));
    }

    @Test
    public void dependencyRejectNullConsumer() {
        Object o = new Object();
        Dependency d = new SystemDependency(o);
        exception.expect(NullPointerException.class);
        d.depend(null);
    }

}
