# mc-bukkit-default-loader

This module provides simple module loading capabilities which should fit a wide
variety of use cases. This is the module which is bootstrapped by default on
Bukkit servers.

# Behavior

This module exposes one command, `/jsmc` which in turn exposes subcommands which
control listing, loading, and unloading of other modules within the root
`node_modules/` directory. This module also loads all other modules by default
on load.
