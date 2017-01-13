package io.ibj.jsmc.api;

/**
 * A dependency lifecycle which enforces a simple contract with an optional close callback. It is recommended to use this
 * lifecycle unless special needs are required which are outside of the scope of this lifecycle's functions.
 *
 * @see DependencyLifecycle
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/5/16
 */
public final class SimpleDependencyLifecycle implements DependencyLifecycle {

    private final Dependency parent;
    private final Object export;
    private final Runnable onClose;
    private boolean closed = false;

    /**
     * Creates a simple lifecycle with single dependency parent, static export object, and optional close callback
     * @param parent dependency parent
     * @param export static lifecycle export
     * @param onClose close callback
     */
    public SimpleDependencyLifecycle(Dependency parent, Object export, Runnable onClose) {
        if (parent == null)
            throw new NullPointerException("Parent dependency cannot be null!");
        this.parent = parent;
        this.export = export;
        this.onClose = onClose;
    }

    /**
     * Creates a simple lifecycle with asimple dependency parent, and a static export object
     * @param parent dependency parent
     * @param export static lifecycle export
     */
    public SimpleDependencyLifecycle(Dependency parent, Object export) {
        this(parent, export, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getDependencyExports() {
        if (closed) throw new IllegalStateException("Dependency lifecycle was already closed");
        return export;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dependency getParentDependency() {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        if (closed) throw new IllegalStateException("Dependency lifecycle was already closed");
        closed = true;
        if (onClose != null)
            onClose.run();
    }
}
