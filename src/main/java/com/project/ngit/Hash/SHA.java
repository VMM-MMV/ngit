package com.project.ngit.Hash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The SHA class provides utility methods for computing SHA-256 hashes.
 */
public class SHA {
    /**
     * Computes the SHA-256 hash of the given input string.
     *
     * @param input the input string to hash
     * @return the computed hash as a hexadecimal string
     */
    public static String computeSHA(String input) {
        MessageDigest md = getAlgorithm("SHA-256");

        assert md != null;
        byte[] hash = md.digest(input.getBytes());
        return bytesToHex(hash);
    }

    /**
     * Retrieves a MessageDigest instance for the specified algorithm.
     *
     * @param algorithm the name of the algorithm to retrieve
     * @return a MessageDigest instance or null if the algorithm is not available
     */
    private static MessageDigest getAlgorithm(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Converts an array of bytes into a hexadecimal string.
     *
     * @param bytes the byte array to convert
     * @return the resulting hexadecimal string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Computes the SHA-256 hash of the contents of the file at the specified path.
     *
     * @param pathToFile the path to the file to hash
     * @return the computed hash as a hexadecimal string
     */
    public static String fileToSHA(String pathToFile) {
        String file = getStringFromFile(pathToFile);
        return computeSHA(file);
    }

    /**
     * Reads the contents of a file into a string.
     *
     * @param pathToFile the path to the file to read
     * @return the file contents as a string
     * @throws RuntimeException if an I/O error occurs
     */
    public static String getStringFromFile(String pathToFile) {
        try {
            return new String(Files.readAllBytes(Path.of(pathToFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
