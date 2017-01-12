var plugin = require("mc-bukkit-plugin")
var c = require("mc-bukkit-consts")
var cmd = require("mc-bukkit-command")
var settings = require("../settings")

var manager = plugin.plugin.dependencyManager

var subcommands

function sendHelp(sender) {
    var s = sender.sendMessage
    var g = c.ChatColor.GRAY
    var lg = c.ChatColor.LIGHT_GRAY
    var w = c.ChatColor.WHITE
    s(g + "==== /jsmc help ====")
    for (var cmd in subcommands) {
        if (!sender.hasPermission(cmd.perm))
            continue
        s(cmd.usage + lg + " - " + w + cmd.desc)
        s("     " + g + cmd.aliases.join(", "))
    }
}
subcommands = [
    {
        name: "list",
        aliases: ["l", "s", "show", "ps"],
        desc: "Lists the modules known to the jsmc system",
        usage: c.ChatColor.GRAY + "Usage: /jsmc list",
        perm: "jsmc.list",
        cmdHandler: (sender, args) => {
            s = sender.sendMessage
            s(c.ChatColor.GREEN + "Active jsmc modules:")
            s(c.ChatColor.GREEN + manager.getLoadedModules().join(c.ChatColor.GRAY + ", " + c.ChatColor.GREEN))
            s(c.ChatColor.RED + "Inactive jsmc modules:")
            s(c.ChatColor.RED + manager.getUnloadedModules().join(c.ChatColor.GRAY + ", " + c.ChatColor.RED))
        },
        tabHandler: (sender, args) => {
            return []
        }
    },
    {
        name: "enable", aliases: ["e", "s", "start"],
        desc: "Enables different modules in jsmc",
        usage: c.ChatColor.GRAY + "Usage: /jsmc enable <module...>",
        perm: "jsmc.enable",
        cmdHandler: (sender, args) => {
            if (cmd.length == 0) {
                sender.sendMessage(usage)
                return
            }
            for (var module in args) {
                try {
                    manager.load(module)
                    sender.sendMessage(c.ChatColor.GREEN + "Loaded module " + module + "'!")
                } catch (e) {
                    sender.sendMessage(c.ChatColor.RED + "Failed to load module '" + module + "' with exception '" + e.getMessage() + "'!")
                }
            }
        },
        tabHandler: (sender, args) => {
            var filter
            if (cmd.length != 0)
                filter = (test) => test.startsWith(cmd[cmd.length - 1])
            else
                filter = (test) => true

            return c.getUnloadedModules().filter(filter)
        }
    },
    {
        name: "disable",
        aliases: ["d", "stop"],
        desc: "Disables different modules in jsmc",
        usage: c.ChatColor.GRAY + "Usage: /jsmc disable <module...>",
        perm: "jsmc.disable",
        cmdHandler: (sender, args) => {
            if (cmd.length == 0) {
                sender.sendMessage(usage)
                return
            }
            // todo - real question, should we be able to disable this loader?
            for (var module in args) {
                try {
                    manager.unload(module)
                    sender.sendMessage(c.ChatColor.GREEN + "Unloaded module '" + module + "'!")
                } catch (e) {
                    sender.sendMessage(c.ChatColor.RED + "Failed to unload module '" + module + "' with exception '" + e.getMessage() + "'!")
                }
            }
        },
        tabHandler: (sender, args) => {
            var filter
            if (cmd.length != 0)
                filter = (test) => test.startsWith(cmd[cmd.length - 1])
            else
                filter = (test) => true
            // todo - if not able to unload the loader, then it needs filtered out too
            return c.getLoadedModules().filter(filter)
        }
    },
    {
        name: "help",
        aliases: ["h", "?"],
        desc: "Displays /jsmc help",
        usage: c.ChatColor.GRAY + "Usage: /jsmc help",
        perm: "jsmc.help",
        cmdHandler: sendHelp,
        tabHandler: () => { return [] }
    }
]

var jsmc = cmd("jsmc")
jsmc.description = "Management command for jsmc based modules"
jsmc.permission = "jsmc.manage"

cmdMap = {}
for (var cmd in subcommands) {
    cmdMap[cmd.name] = cmd
    for (var alias in cmd.aliases) {
        cmdMap[cmd.alias] = cmd
    }
}

jsmc.commandHandler = (sender, label, args) => {
    if (!sender.hasPermission(jsmc.permission)) {
        sender.sendMessage(c.ChatColor.RED + "You do not have permission to use this command!")
        return
    }

    if (args.length == 0) {
        sendHelp(sender)
        return
    }

    cmd = cmdMap[args[0].toLowerCase()]
    if (cmd == null) {
        sender.sendMessage(c.ChatColor.RED + "Subcommand '" + args[0] + "' not found!")
        sendHelp(sender)
        return
    }
    
    subargs = args.slice(1) 
    return cmd.cmdHandler(sender, subargs) && true
}

jsmc.tabHandler = (sender, label, args) => {
    if (!sender.hasPermission(jsmc.permission)) {
        return []
    }

    if (args.length <= 1) {
        var filter
        if (args.length == 0)
            filter = (cmd) => sender.hasPermission(cmd.perm)
        else
            filter = (cmd) => cmd.startsWith(args[0].toLowerCase()) && sender.hasPermission(cmd.perm)
        return cmdMap.keys().filter(filter)
    }

    cmd = cmdMap[args[0].toLowerCase()]
    if (cmd == null)
        return []
    if (!sender.hasPermission(cmd.perm))
        return []

    return cmd.tabHandler(sender, args.slice(1))
}

var modules = plugin.moduleResolver.getLoadableModules()

// we can't enable ourselves since doing so would 'double enable us'
var isUs = (module) => module == "mc-bukkit-default-loader"

var shouldEnable
if ('whitelistModules' in modules)
    shouldEnable = (module) => modules.whitelistModules.contains(module) && !isUs
else if ('blacklistModules' in modules)
    shouldEnable = (module) => !modules.blacklistModules.contains(module) && !isUs
else
    shouldEnable = () => !isUs
for (var module in modules) {
    if (shouldEnable(module))
        manager.load(module)
}
