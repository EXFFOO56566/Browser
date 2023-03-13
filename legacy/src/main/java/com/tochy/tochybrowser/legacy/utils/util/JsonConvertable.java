package com.tochy.tochybrowser.legacy.utils.util;

public interface JsonConvertable {
    String toJsonString();

    boolean fromJsonString(String str);
}
