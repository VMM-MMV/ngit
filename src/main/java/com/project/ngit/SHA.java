package com.project.ngit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA {
    public static String computeSHA256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes());
        return bytesToHex(hash);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        try {
            String input = "Hello, World!";
            String hash = computeSHA256(input);
            System.out.println("SHA-256 Hash: " + hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
