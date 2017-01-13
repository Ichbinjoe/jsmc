package io.ibj.jsmc.api;

/**
 * Represents something which can take exception reports which itself generated for self introspection
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/14/16
 */
public interface Reportable {

    /**
     * Accepts a throwable which is suspected to be caused by whatever object this reportable is responsible for (usually
     * just itself).
     *
     * @param t throwable to report
     */
    void report(Throwable t);

}
