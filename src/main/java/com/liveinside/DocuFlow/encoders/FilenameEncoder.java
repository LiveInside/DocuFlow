package com.liveinside.DocuFlow.encoders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FilenameEncoder {
    public static String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            // Handle the exception (not expected to happen with UTF-8)
            throw new RuntimeException(e);
        }
    }
}
