var Bukkit = Java.type("org.bukkit.Bukkit")

var getPlugin = function(plugin) {
  return Bukkit.getPluginManager().getPlugin(plugin)
}

module.exports = {
  plugin: getPlugin,
  jsmc: getPlugin("jsmc")
}
