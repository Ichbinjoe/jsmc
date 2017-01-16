var runtimeException = Java.type("java.lang.RuntimeException");

module.generator = function () {
    return {
        exports: {},
        close: function () {
            throw new runtimeException()
        }
    }
};

module.onError = function (ex) {
    throw new runtimeException()
};