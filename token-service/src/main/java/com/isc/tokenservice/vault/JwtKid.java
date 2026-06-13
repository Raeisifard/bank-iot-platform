package com.isc.tokenservice.vault;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class JwtKid {
    private static final String kidFormat = "yyMMddHHmmss";

    public static String newKid() {
        Instant now = Instant.now();
        return newKid(now);
    }

    public static String newKid(Instant now) {
        return "bank-" +
                DateTimeFormatter.ofPattern(kidFormat)
                        .withZone(ZoneOffset.UTC)
                        .format(now);
    }
}
