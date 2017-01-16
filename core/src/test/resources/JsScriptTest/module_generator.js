var hook = require("test");
var Assert = Java.type("org.junit.Assert");

module.exports = {
    key: "ignore me!!!"
};

module.generator = function () {
    return {
        exports: {
            key: "value"
        },
        close: function () {
            if (hook.closeCalled)
                Assert.fail("Generator close called twice!");
            hook.closeCalled = true;
        }
    }
};