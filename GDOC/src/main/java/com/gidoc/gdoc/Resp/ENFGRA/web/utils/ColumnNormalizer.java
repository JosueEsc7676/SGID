package com.gidoc.gdoc.Resp.ENFGRA.web.utils;

public class ColumnNormalizer {

    public static String normalize(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
