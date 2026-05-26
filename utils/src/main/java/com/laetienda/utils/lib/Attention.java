package com.laetienda.utils.lib;

public enum Attention {
    PARSE_LONG_EXCEPTION(0xA001, "Failed to parse string to long. $error: %s"),
    INVALID_PARAM(0xA002, "Invalid parameter along uri. $param: %s"),
    CLASS_CAST_EXCEPTION(0xA003, "Failed to cast clazz name. | $clazzName: %s | $exception: %s");

    private final int code;
    private final String message;

    Attention(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage(Object... args) {
        return String.format(message, args);
    }

    public String getError(Object... args) {
        return String.format("%x -> %s", this.code, getMessage(args));
    }
}
