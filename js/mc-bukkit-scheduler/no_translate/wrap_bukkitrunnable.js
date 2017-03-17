var c = require("mc-bukkit-consts")
var BukkitRunnableExtender = Java.extend(c.BukkitRunnable)

module.exports = function(r) {
  return new BukkitRunnableExtender() {
    run: function() {r();}
  }
}
