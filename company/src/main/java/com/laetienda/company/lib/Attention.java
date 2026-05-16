package com.laetienda.company.lib;

import java.util.Arrays;
import java.util.IllegalFormatException;

public enum Attention {

    VANITY_URL_ALREADY_EXISTS(
            0xA301,
            "%x -> Vanity URL already exists | $url: %s"
    );

    final private int code;
    final private String messageFormat;

    Attention(int code, String messageFormat) {
        this.code = code;
        this.messageFormat = messageFormat;
    }

    public int getCode() {
        return code;
    }

    public String get(Object... variables){
        try {
            return String.format(messageFormat, getCode(), Arrays.toString(variables));
        }catch(IllegalFormatException e){
            return String.format("%x", code) + " -> " + this.messageFormat;
        }
    }
}
