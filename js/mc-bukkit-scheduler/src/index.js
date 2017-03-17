var c = require("mc-bukkit-consts")
var plugin = require("mc-bukkit-plugin")
var wrapRunnable = require("../no_translate/wrap_bukkitrunnable.js")

var synch = {
    now: (br) => br.runTask(plugin.jsmc),
    later: (br, delay) => br.runTaskLater(plugin.jsmc, delay),
    timer: (br, delay, period) => br.runTaskTimer(plugin.jsmc, delay, period)
}

var asynch = {
  now: (br) => br.runTaskAsynchronously(plugin.jsmc),
  later: (br, delay) => br.runTaskLaterAsynchronously(plugin.jsmc, delay),
  timer: (br, delay, period) => br.runTaskTimerAsynchronously(
    plugin.jsmc, delay, period)
}

function wrapOnEnd(runnable, end) {
  return () => {
    try {
      runnable()
    } finally {
      end()
    }
  }
}

function createScheduler(bindings) {
  var cleanupRunnables = {}
  var create = function(invoker) {
    var internalState = {
    }
  
    var cleanupFunc = () => {
      internalState.task.cancel()
    }

    var statusObject = {
      cancel: () => {
        cleanupFunc()
        delete cleanupRunnables[internalState] 
      }
    }

    Object.defineProperty(statusObject, "scheduled", {
      get: () => internalState.task.getTaskId() >= 0
    })

    Object.defineProperty(statusObject, "taskId", {
      get: () => internalState.task.getTaskId()
    })

    internalState.task = invoker(statusObject.cancel)

    cleanupRunnables[internalState] = cleanupFunc

    return statusObject
  }
  return {
    close: () => {
      for (key in cleanupRunnables) {
        cleanupRunnables[key].cancel()
        delete cleanupRunnables[key]
      }
    },
    exports: { 
      now: (r) => {
        return create(cleanup => bindings.now(wrapRunnable(
          wrapOnEnd(r, cleanup)))) 
      },
      later: (r, delay) => {
        return create(cleanup => bindings.later(wrapRunnable(
          wrapOnEnd(r, cleanup)), delay))
      },

      timer: (r, delay, period) => {
        return create(cleanup => bindings.timer(wrapRunnable(r), 
                                                delay, period))
      }
    }
  }
}

module.generator = () => {
  var syncSched = createScheduler(synch)
  var asyncSched = createScheduler(asynch)
  return {
    exports: {
      sync: syncSched.exports,
      async: asyncSched.exports
    },
    close: () => {
      syncSched.close()
      asyncSched.close()
    }
  }
}
