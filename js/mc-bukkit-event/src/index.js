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
      if (!(Class.class.isInstance(eventspec))) {
        const eventType = eventspec.event
        const priority = eventspec.priority || c.EventPriority.NORMAL
        const ignoreCancelled = eventspec.ignoreCancelled || true
      } else {
        const eventType = eventspec
        const priority = c.EventPriority.NORMAL
        const ignoreCancelled = true
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
