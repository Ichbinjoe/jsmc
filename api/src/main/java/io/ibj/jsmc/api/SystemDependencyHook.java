package io.ibj.jsmc.api;

/**
 * Exposure so other java plugins can hook into jsmc and supply themselves as modules
 * <p>
 * Valid module label regex: '^[a-z-_0-9]*$'
 * <p>
 * // todo - figure out whether this is actually the contract we want to hold for plugins/extensions
 * Note, dependency hooks currently cannot be unregistered once they are registered. This is due to the complicated
 * dependency chain that might break if a providing plugin yanked its dependency from being require'd. Granted, if
 * a resource doesn't actively resolve the dependency nothing breaks, but if a script is reloaded, then a module that
 * was once there no longer is. This is up to review, and comments are welcome!
 *
 * @author Joseph Hirschfeld [Ichbinjoe] (joe@ibj.io)
 * @since 1/13/17
 */
public interface SystemDependencyHook {

    /**
     * Adds a dependency to the system dependency manager. Will not overwrite a dependency if it already exists under
     * the same label
     *
     * @param label  label for the new dependency
     * @param object dependency object for resolution
     * @throws IllegalArgumentException if the label passed does not follow module naming conventions
     * @throws IllegalStateException    if the label passed has already been registered internally
     */
    void add(String label, Dependency object);

    /**
     * Sets a dependency in the system dependency manager. This method will overwrite a dependency if it is already
     * registered within the manager. This method should be rarely used. If the module previously existed, this method
     * will also 'reload' the module so all depending objects will update their references. This will also mean that
     * scripts may loose their state, since they must be reevaluated
     *
     * @param label  label for the new dependency
     * @param object dependency object for resolution
     * @throws IllegalArgumentException if the label passed does not follow module naming conventions
     * @see SystemDependencyHook#add(String, Dependency)
     */
    void set(String label, Dependency object);

    /**
     * Sets a dependency in the system dependency manager. This method will overwrite a dependency if it is already
     * registered within the manager. This method also will not reload any depending modules on the old dependency,
     * allowing for stale/old dependencies until the scripts are either reloaded by a
     * {@link SystemDependencyHook#set(String, Dependency)} call, or a reload trigger from somewhere else. This method
     * should almost never be used.
     *
     *
     * @deprecated no unit tests, no impact at the moment
     * @param label  label for the new dependency
     * @param object dependency object for resolution
     * @throws IllegalArgumentException if the label passed does not follow module naming conventions
     * @see SystemDependencyHook#add(String, Dependency)
     */
    void setSilently(String label, Dependency object);

    /**
     * Returns whether or not a label has already been registered within the system dependency manager
     *
     * @param label label to test conditionally whether it has been registered
     * @return whether the label has a dependency registered to it
     */
    boolean has(String label);
}
