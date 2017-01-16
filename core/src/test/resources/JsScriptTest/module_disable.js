var hook = require("test");
var Assert = Java.type("org.junit.Assert");

module.disable = function() {
    if (hook.closeCalled)
        Assert.fail("Disable callback called twice!");
    hook.closeCalled = true
};