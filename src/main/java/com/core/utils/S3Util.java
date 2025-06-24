package com.core.utils;

public class S3Util {

    public static String generateS3Key(String clientId, String filename) {
        return String.format("%s/%s",
                clientId,
                filename);
    }
}
