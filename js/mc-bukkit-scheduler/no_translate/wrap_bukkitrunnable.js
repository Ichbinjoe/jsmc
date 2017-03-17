var c = require("mc-bukkit-consts")
var BukkitRunnableExtender = Java.extend(c.BukkitRunnable)

module.exports = function(r) {
  print(r)
  return new BukkitRunnableExtender() {
    run: function() {r();}
  }
}
