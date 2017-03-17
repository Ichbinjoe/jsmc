const plugin = require("mc-bukkit-plugin")
const c = require("mc-bukkit-consts")
const cmd = require("mc-bukkit-command")
const settings = require("../settings")

const HashMap = Java.type("java.util.HashMap")
const ArrayList = Java.type("java.util.ArrayList")
const List = Java.type("java.util.List")

const EmptyList = new ArrayList()

var manager = plugin.jsmc.dependencyManager 
var subcommands

function sendHelp(sender) {
    const lg = c.ChatColor.GRAY
    const g = c.ChatColor.DARK_GRAY
    const w = c.ChatColor.WHITE
    sender.sendMessage(g + "==== /jsmc help ====")
    for (var i in subcommands) {
        var cmd = subcommands[i]
        if (!sender.hasPermission(cmd.perm)) continue
        sender.sendMessage(cmd.usage + lg + " - " + w + cmd.desc)
        sender.sendMessage("     " + g + cmd.aliases.join(", "))
    }
}

function joinStrCol(col, sep) {
  let a = ""
  for (let i in col) {
    if (a != "")
      a += sep
    a += col[i] 
  }
  return a
}

function filter(col, pred) {
  let a = []
  let itr = col.iterator()
  while (itr.hasNext()) {
    let e = itr.next()
    if (pred(e))
      a.push(e)
  }
  return a
}

function mapJCol(col, func) {
  let a = []
  let itr = col.iterator()
  while (itr.hasNext())
    a.push(func(itr.next()))
  return a
}

function idMap(entries) {
  return mapJCol(entries, e => e.getIdentifier())
}

function loadableModuleNames() {
  return plugin.jsmc.getLoadableModules()
}

function loadedModuleNames() {
  return idMap(manager.getLoadedModules())
}

function unloadedModuleNames() {
  let lmn = loadedModuleNames()
  return filter(loadableModuleNames(), a => lmn.indexOf(a) == -1)
}

function findLoadedModule(name) {
  let lm = manager.getLoadedModules()
  let itr = lm.iterator()
  while (itr.hasNext()) {
    let e = itr.next()
    if (e.getIdentifier().equals(name))
      return e
  }
  return null
}


subcommands = [
    {
        name: "list",
        aliases: ["l", "s", "show", "ps"],
        desc: "Lists the modules known to the jsmc system",
        usage: c.ChatColor.GRAY + "Usage: /jsmc list",
        perm: "jsmc.list",
        cmdHandler: (sender, args) => {
            sender.sendMessage(c.ChatColor.GREEN + "Active jsmc modules:")
            sender.sendMessage(c.ChatColor.GREEN + 
              joinStrCol(loadedModuleNames(), c.ChatColor.GRAY + ", " + c.ChatColor.GREEN))
            sender.sendMessage(c.ChatColor.RED + "Inactive jsmc modules:")
            sender.sendMessage(c.ChatColor.RED + 
              joinStrCol(unloadedModuleNames(), c.ChatColor.GRAY + ", " + c.ChatColor.RED))
        },
        tabHandler: (sender, args) => {
            return EmptyList
        }
    },
    {
        name: "enable", aliases: ["e", "s", "start"],
        desc: "Enables different modules in jsmc",
        usage: c.ChatColor.GRAY + "Usage: /jsmc enable <module...>",
        perm: "jsmc.enable",
        cmdHandler: (sender, args) => {
            if (args.length == 0) {
                sender.sendMessage(usage)
                return
            }
            for (var m in args) {
              const module = args[m]
              try {
                manager.load(module)
                sender.sendMessage(c.ChatColor.GREEN + "Loaded module " + module + "'!")
              } catch (e) {
                sender.sendMessage(c.ChatColor.RED + "Failed to load module '" + module + "' with exception '" + e.getMessage() + "'!")
              }
            }
        },
        tabHandler: (sender, args) => {
          let pred
          if (args.length != 0)
            pred = test => test.startsWith(args[args.length - 1])
          else
            pred = test => true
          return Java.to(unloadedModuleNames().filter(pred), List)
        }
    },
    {
        name: "disable",
        aliases: ["d", "stop"],
        desc: "Disables different modules in jsmc",
        usage: c.ChatColor.GRAY + "Usage: /jsmc disable <module...>",
        perm: "jsmc.disable",
        cmdHandler: (sender, args) => {
            if (args.length == 0) {
                sender.sendMessage(usage)
                return
            }
            // todo - real question, should we be able to disable this loader?
            for (var module in args) {
              const modName = args[module]
                try {
                    let mod = findLoadedModule(modName)
                    if (mod == null) {
                      sender.sendMessage(c.ChatColor.RED + "Module " + modName + " not loaded!")
                      continue
                    }
                    manager.unload(mod)
                    sender.sendMessage(c.ChatColor.GREEN + "Unloaded module '" + modName + "'!")
                } catch (e) {
                    sender.sendMessage(c.ChatColor.RED + "Failed to unload module '" + modName + "' with exception '" + e.getMessage() + "'!")
                }
            }
        },
        tabHandler: (sender, args) => {
            let pred
            if (args.length != 0)
                pred = (test) => test.startsWith(args[args.length - 1])
            else
                pred = (test) => true
            // todo - if not able to unload the loader, then it needs filtered out too
            return Java.to(loadedModuleNames().filter(pred), List)
        }
    },
    {
        name: "help",
        aliases: ["h", "?"],
        desc: "Displays /jsmc help",
        usage: c.ChatColor.GRAY + "Usage: /jsmc help",
        perm: "jsmc.help",
        cmdHandler: sendHelp,
        tabHandler: () => { return EmptyList }
    }
]

var jsmc = cmd("jsmc")
jsmc.description = "Management command for jsmc based modules"
jsmc.permission = "jsmc.manage"
let cmdMap = new HashMap()
for (var i in subcommands) {
    let cmd = subcommands[i]
    cmdMap.put(cmd.name, cmd)
    for (var alias in cmd.aliases) {
      cmdMap.put(cmd.aliases[alias], cmd)
    }
}

const Arrays = Java.type("java.util.Arrays")

jsmc.commandHandler = (sender, label, args) => {
    if (!sender.hasPermission(jsmc.permission)) {
        sender.sendMessage(c.ChatColor.RED + "You do not have permission to use this command!")
        return
    }

    if (args.length == 0) {
        sendHelp(sender)
        return
    }
    
    let k = args[0].toLowerCase()

    let cmd = cmdMap.get(k)
    if (cmd == null) {
        sender.sendMessage(c.ChatColor.RED + "Subcommand '" + args[0] + "' not found!")
        sendHelp(sender)
        return
    }

    let subargs = Arrays.copyOfRange(args, 1, args.length)
    return cmd.cmdHandler(sender, subargs) && true
}

const Collectors = Java.type("java.util.stream.Collectors")

jsmc.tabHandler = (sender, label, args) => {
    if (!sender.hasPermission(jsmc.permission)) {
        return EmptyList
    }

    if (args.length <= 1) {
        let filter
        if (args.length == 0)
            filter = (cmd) => sender.hasPermission(cmd.perm)
        else
            filter = (cmd) => cmd.startsWith(args[0].toLowerCase()) && sender.hasPermission(cmd.perm)
        return cmdMap.keySet().stream().filter(filter).collect(Collectors.toList())
    }

    let cmd = cmdMap[args[0].toLowerCase()]
    if (cmd == null)
        return EmptyList
    if (!sender.hasPermission(cmd.perm))
        return EmptyList

    let subargs = Arrays.copyOfRange(args, 1, args.length)
    return cmd.tabHandler(sender, subargs)
}

var modules = loadableModuleNames()

// we can't enable ourselves since doing so would 'double enable us'
const isUs = (module) => module == "mc-bukkit-default-loader"

let shouldEnable
if ('whitelistModules' in modules)
    shouldEnable = (module) => modules.whitelistModules.contains(module) && !isUs(module)
else if ('blacklistModules' in modules)
    shouldEnable = (module) => !modules.blacklistModules.contains(module) && !isUs(module)
else
    shouldEnable = (module) => !isUs(module)
for (var module in modules) {
    if (shouldEnable(module))
        manager.load(module)
}
