var hook = require("test");
var Assert = Java.type("org.junit.Assert");
var runtimeException = Java.type("java.lang.RuntimeException");

module.disable = function() {
    throw new runtimeException()
};

module.onError = function (ex) {
    if (hook.exceptionReported)
        Assert.fail("Exception reported multiple times!");
    if (!(ex instanceof Java.type("io.ibj.jsmc.api.exceptions.ModuleExecutionException")))
        Assert.fail("Exception was not of type ModuleExecutionException");
    hook.exceptionReported = true;
};
