package com.project.ngit;

import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Zlib {

    public static String compress(String data) throws IOException {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        deflater.end();

        return Base64.getEncoder().encodeToString(output);
    }

    public static String decompress(String data) throws IOException, DataFormatException {
        byte[] input = Base64.getDecoder().decode(data);
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        inflater.end();

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException, DataFormatException {
        String inputString = "Text to be compressed using zlib in Java";
        String compressed = Zlib.compress(inputString);
        System.out.println("Compressed: " + compressed);
        String decompressed = Zlib.decompress(compressed);
        System.out.println("Decompressed: " + decompressed);
    }
}
