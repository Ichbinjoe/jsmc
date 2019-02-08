package io.ibj.jsmc.runtime;

/**
 * Metainterface for plugins wishing to be consistent as a RuntimeHost.
 */
public interface RuntimeHost {
    /**
     * Returns the active system runtime.
     * @return active JsRuntime
     */
    JsRuntime getRuntime();
}
