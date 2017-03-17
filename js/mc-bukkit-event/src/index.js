const bacon = require("baconjs")
const plugin = require("mc-bukkit-plugin")
const c = require("mc-bukkit-consts")

const Class = Java.type("java.lang.Class")

const GenericListener = Java.extend(c.Listener, {})

module.generator = () => {
  const pendingStreams = []
  return {
    close: () => {
      let streamClose
      while ((streamClose = pendingStreams.pop()) != null)
        streamClose()
    },
    exports: (eventspec) => {
      if (eventspec.class != null) {
        eventspec = eventspec.class
      }
      let eventType
      let priority
      let ignoreCancelled
      if (!(Class.class.isInstance(eventspec))) {
        eventType = eventspec.event
        priority = eventspec.priority || c.EventPriority.NORMAL
        ignoreCancelled = eventspec.ignoreCancelled || true
      } else {
        eventType = eventspec
        priority = c.EventPriority.NORMAL
        ignoreCancelled = true
      }

      return bacon.fromBinder(sink => {
        const listener = new GenericListener() // new listener
        const MyHandlerType = Java.extend(c.EventExecutor, (listener, event) =>
          sink(new bacon.Next(event))
        )

        c.Bukkit.getPluginManager().
          registerEvent(eventType, listener, priority, new MyHandlerType(), 
            plugin.jsmc, ignoreCancelled) 

        let cleanup = () => {
          c.HandlerList.unregisterAll(listener)
        }
        
        pendingStreams.push(cleanup)
        return cleanup
      })
    }
  }
}
