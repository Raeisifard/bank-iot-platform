package com.isc.tokenservice.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

    private HashUtils() {
    }

    public static String sha256(String value) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] digest =
                    md.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            for (byte b : digest) {
                sb.append(
                        String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e);
        }
    }
}