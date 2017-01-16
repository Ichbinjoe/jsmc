package io.ibj.jsmc.core.dependencies;

import io.ibj.jsmc.api.*;
import io.ibj.jsmc.api.exceptions.ModuleExecutionException;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests {@link LogicalModule}
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/15/17
 */
public class LogicalModuleTest extends DependencyContractTest {

    @Test
    public void testDependReturnsInternalValue() throws Exception {
        LogicalModule logicalModule = new LogicalModule();
        Dependency innerDependency = mock(Dependency.class);

        DependencyLifecycle innerLifecycle = mock(DependencyLifecycle.class);
        Object innerObject = new Object();
        when(innerLifecycle.getDependencyExports()).thenReturn(innerObject);

        when(innerDependency.depend(any())).thenReturn(innerLifecycle);
        logicalModule.setInternalDependency(innerDependency);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = logicalModule.depend(consumer);

        assertEquals(innerObject, lifecycle.getDependencyExports());
    }

    @Test
    public void testDependCloseAlsoClosesInternalDependencyLifecycle() throws Exception {
        LogicalModule logicalModule = new LogicalModule();
        Dependency innerDependency = mock(Dependency.class);

        DependencyLifecycle innerLifecycle = mock(DependencyLifecycle.class);
        Object innerObject = new Object();
        when(innerLifecycle.getDependencyExports()).thenReturn(innerObject);

        when(innerDependency.depend(any())).thenReturn(innerLifecycle);
        logicalModule.setInternalDependency(innerDependency);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = logicalModule.depend(consumer);
        // verifies that the logical module has the lifecycle
        verify(innerDependency, times(1)).depend(logicalModule);
        lifecycle.close();
        verify(innerLifecycle, times(1)).close();
    }

    @Test
    public void testLifecycleCloseProperlyReportsExceptionsOnReportableInnerDependency() throws Exception {
        LogicalModule logicalModule = new LogicalModule();
        ReportableDependency innerDependency = mock(ReportableDependency.class);

        DependencyLifecycle innerLifecycle = mock(DependencyLifecycle.class);
        Object innerObject = new Object();
        when(innerLifecycle.getParentDependency()).thenReturn(innerDependency);
        when(innerLifecycle.getDependencyExports()).thenReturn(innerObject);
        RuntimeException exception = new RuntimeException("I'm a bad implementation, and messed up!");
        doThrow(exception).when(innerLifecycle).close();

        when(innerDependency.depend(any())).thenReturn(innerLifecycle);
        logicalModule.setInternalDependency(innerDependency);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = logicalModule.depend(consumer);
        lifecycle.close();
        verify(innerDependency, times(1)).report(any(ModuleExecutionException.class));
    }

    @Test
    public void testLifecycleCloseThrowsExceptionsOnNonReportableInnerDependency() throws Exception {
        LogicalModule logicalModule = new LogicalModule();
        Dependency innerDependency = mock(Dependency.class);

        DependencyLifecycle innerLifecycle = mock(DependencyLifecycle.class);
        Object innerObject = new Object();
        when(innerLifecycle.getParentDependency()).thenReturn(innerDependency);
        when(innerLifecycle.getDependencyExports()).thenReturn(innerObject);
        doThrow(new RuntimeException("I'm a bad implementation, and messed up!")).when(innerLifecycle).close();

        when(innerDependency.depend(any())).thenReturn(innerLifecycle);
        logicalModule.setInternalDependency(innerDependency);

        DependencyConsumer consumer = mock(DependencyConsumer.class);

        DependencyLifecycle lifecycle = logicalModule.depend(consumer);

        // todo - this needs cleaned up, not a blackbox knowledge it throws a runtime!!!
        exception.expect(RuntimeException.class);
        lifecycle.close();
    }

    @Override
    public Dependency createNewTestable() throws Exception {
        LogicalModule logicalModule = new LogicalModule();
        Dependency innerDependency = mock(Dependency.class);

        DependencyLifecycle innerLifecycle = mock(DependencyLifecycle.class);
        when(innerLifecycle.getDependencyExports()).thenReturn(new Object());

        // a logical module **should** only be the only depender on a logical module, so returning one should be alright
        when(innerDependency.depend(any())).thenReturn(innerLifecycle);
        logicalModule.setInternalDependency(innerDependency);
        return logicalModule;
    }

    public interface ReportableDependency extends Dependency, Reportable {
    }

    @After
    public void validate() {
        validateMockitoUsage();
    }
}