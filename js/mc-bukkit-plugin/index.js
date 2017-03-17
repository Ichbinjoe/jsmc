var Bukkit = Java.type("org.bukkit.Bukkit")

var getPlugin = function(plugin) {
  return Bukkit.getPluginManager().getPlugin(plugin)
}

var jsmc = getPlugin("jsmc")

module.exports = {
  plugin: getPlugin,
  jsmc: jsmc
}
