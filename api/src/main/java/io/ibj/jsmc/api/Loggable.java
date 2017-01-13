package io.ibj.jsmc.api;

import java.util.logging.Logger;

/**
 * A module which is capable of accepting logs
 *
 * @author Joseph Hirschfeld (Ichbinjoe) [joe@ibj.io]
 * @since 9/14/16
 */
public interface Loggable {

    /**
     * Returns the object's logger
     * @return logger
     */
    Logger getLogger();

}
