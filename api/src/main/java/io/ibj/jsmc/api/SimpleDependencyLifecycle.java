package io.ibj.jsmc.api;

// todo - javadocs

/**
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public class SimpleDependencyLifecycle implements DependencyLifecycle {

    private final Dependency parent;
    private final Object export;
    private final Runnable onClose;
    private boolean closed = false;

    public SimpleDependencyLifecycle(Dependency parent, Object export, Runnable onClose) {
        this.parent = parent;
        this.export = export;
        this.onClose = onClose;
    }

    public SimpleDependencyLifecycle(Dependency parent, Object export) {
        this(parent, export, null);
    }

    @Override
    public Object getDependencyExports() {
        if (closed) throw new IllegalStateException("Dependency lifecycle was already closed");
        return export;
    }

    @Override
    public Dependency getParentDependency() {
        return parent;
    }

    @Override
    public void close() throws Exception {
        if (closed) throw new IllegalStateException("Dependency lifecycle was already closed");
        closed = true;
        if (onClose != null)
            onClose.run();
    }
}
