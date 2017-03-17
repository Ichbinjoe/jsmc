var bacon = require("baconjs")
var plugin = require("mc-bukkit-plugin")
var c = require("mc-bukkit-consts")
var Class = Java.type("java.lang.Class")

var GenericListener = Java.extend(c.Listener, {})

module.generator = () => {
  var pendingStreams = []
  return {
    close: () => {
      var streamClose
      while ((streamClose = pendingStreams.pop()) != null)
        streamClose()
    },
    exports: (eventspec) => {
      if (eventspec.class != null) {
        eventspec = eventspec.class
      }
      if (!(Class.class.isInstance(eventspec))) {
        var eventType = eventspec.event
        var priority = eventspec.priority || c.EventPriority.NORMAL
        var ignoreCancelled = eventspec.ignoreCancelled || true
      } else {
        var eventType = eventspec
        var priority = c.EventPriority.NORMAL
        var ignoreCancelled = true
      }

      return bacon.fromBinder(sink => {
        var listener = new GenericListener() // new listener
        var MyHandlerType = Java.extend(c.EventExecutor, (listener, event) =>
          sink(new bacon.Next(event))
        )

        c.Bukkit.getPluginManager().
          registerEvent(eventType, listener, priority, new MyHandlerType(), 
                        plugin.jsmc, ignoreCancelled) 

        var cleanup = () => {
          c.HandlerList.unregisterAll(listener)
        }
        
        pendingStreams.push(cleanup)
        return cleanup
      })
    }
  }
}
