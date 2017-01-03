var bacon = require("baconjs")
var plugin = require("mc-bukkit-plugin")
var c = require("mc-bukkit-consts")

var commandMapField = c.Bukkit.getPluginManager().getClass().getDeclaredField("commandMap")
commandMapField.setAccessible(true)
var commandMap = commandMapField.get(c.Bukkit.getPluginManager())

module.generator = () => {
  var commandTeardowns = []
  return {
    close: () => {
      var cmdClose
      while ((cmdClose = commandTeardowns.pop()) != null)
        cmdClose()
    },
    exports: (commandname) => {
      var internalDat = {
        registered: false,
        cmdHandler: null,
        tabHandler: null
      }
      var CmdType = Java.extend(c.Command, {
        execute: (sender, label, args) => {
          if (internalDat.cmdHandler != null) {
            return internalDat.cmdHandler(sender, args, label) && true
          }
          return false
        },
        tabComplete: (sender, alias, args, location) => {
          if (internaldat.tabHandler != null) {
            return internalDat.tabHandler(sender, args, label, command)
          }
          return c.Bukkit.matchPlayer(args[args.length - 1])
        }
      })

      var plgnCmd = new CmdType(commandname)
      var commandBuilder = {
        destroy: () => {
          plgnCmd.unregister(commandMap)
        } 
      }

      function registerCommand() {
        if (internalDat.registered) return
        commandMap.register(commandname, plgnCmd)
        internalDat.registered = true
      }

      Object.defineProperty(commandBuilder, "description", {
        get: () => plgnCmd.getDescription(),
        set: (desc) => plgnCmd.setDescription(desc)
      })

      Object.defineProperty(commandBuilder, "label", {
        get: () => plgnCmd.getLabel(),
        set: (lbl) => plgnCmd.setLabel(lbl)
      })

      Object.defineProperty(commandBuilder, "name", {
        get: () => plgnCmd.getName(),
        set: (name) => plgnCmd.setName(name)
      })

      Object.defineProperty(commandBuilder, "permission", {
        get: () => plgnCmd.getPermission(),
        set: (perm) => plgnCmd.setPermission(perm)
      })

      Object.defineProperty(commandBuilder, "permissionMessage", {
        get: () => plgnCmd.getPermissionMessage(),
        set: (permMsg) => plgnCmd.setPermissionMessage(permMsg)
      })

      Object.defineProperty(commandBuilder, "usage", {
        get: () => plgnCmd.getUsage(),
        set: (usage) => plgnCmd.setUsage(usage)
      })

      Object.defineProperty(commandBuilder, "aliases", {
        get: () => Java.from(plgnCmd.getAliases()),
        set: (aliases) => plgnCmd.setAliases(Java.to(aliases, "List<String>"))
      })

      Object.defineProperty(commandBuilder, "registered", {
        get: () => internalDat.registered
      })

      Object.defineProperty(commandBuilder, "commandHandler", {
        get: () => internalDat.cmdHandler,
        set: (handler) => {
          registerCommand()
          internalDat.cmdHandler = handler
        }
      })

      Object.defineProperty(commandBuilder, "tabHandler", {
        get: () => internalDat.tabHandler,
        set: (handler) => {
          registerCommand()
          internalDat.tabHandler = handler
        }
      })
      commandTeardowns.push(commandBuilder.destroy)
      return commandBuilder
    }
  }
}
