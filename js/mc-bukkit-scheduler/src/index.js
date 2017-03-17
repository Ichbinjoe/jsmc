const c = require("mc-bukkit-consts")
const plugin = require("mc-bukkit-plugin")
const wrapRunnable = require("../no_translate/wrap_bukkitrunnable.js")

const synch = {
  now: (br) => br.runTask(plugin.jsmc),
  later: (br, delay) => br.runTaskLater(plugin.jsmc, delay),
  timer: (br, delay, period) => br.runTaskTimer(plugin.jsmc, delay, period)
}

const asynch = {
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
  const cleanupRunnables = {}
  const create = invoker => {
    const internalState = {
    }
  
    const cleanupFunc = () => {
      internalState.task.cancel()
    }

    const statusObject = {
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
  const syncSched = createScheduler(synch)
  const asyncSched = createScheduler(asynch)
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
