package com.gidoc.gdoc.Resp.ENFGRA.web.utils;

import java.util.Map;

public class EnfgraRowValidator {

    public static boolean isValid(Map<String, String> row) {
        return row.containsKey("nip") && row.containsKey("cinfra")
                && !row.get("nip").isBlank()
                && !row.get("cinfra").isBlank();
    }
}
