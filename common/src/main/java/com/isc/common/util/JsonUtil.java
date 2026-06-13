package com.isc.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    public static String toJson(Object object)
            throws Exception {

        return MAPPER.writeValueAsString(object);

    }
}
