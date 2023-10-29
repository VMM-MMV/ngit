package com.project.ngit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA {
    public static String computeSHA(String input) {
        MessageDigest md = getAlgorithm("SHA-256");

        assert md != null;
        byte[] hash = md.digest(input.getBytes());
        return bytesToHex(hash);
    }

    private static MessageDigest getAlgorithm(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static String fileToSHA(String pathToFile) {
        String file = getStringFromFile(pathToFile);
        return computeSHA(file);
    }

    private static String getStringFromFile(String pathToFile) {
        try {
            return new String(Files.readAllBytes(Path.of(pathToFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String input = "Hello, World!";
        String hash = computeSHA(input);
        System.out.println("SHA-256 Hash: " + hash);
    }
}
