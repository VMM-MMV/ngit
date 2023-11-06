package com.project.ngit;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.Deflater;

public class AddCommand {
    static Path ngitPath;
    static String repositoryPath;
    static Map<String, BlobStatus> existingData = new HashMap<>();

    public static void execute(String repositoryPath, String argument) {
        if (argument == null) {
            System.out.println("No argument provided for add command.");
            return;
        }

        AddCommand.repositoryPath = repositoryPath;
        ngitPath = Path.of(repositoryPath, ".ngit");

        existingData = readExistingData(ngitPath.resolve("index/changes.ser"));

        if (argument.equals(".")) {
            processAllFilesInRepository();
        } else {
            processSingleFile(argument);
        }

        saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }

    private static void processAllFilesInRepository() {
        try (Stream<Path> stream = Files.walk(Path.of(repositoryPath))) {
            stream.forEach(AddCommand::processPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void processSingleFile(String argument) {
        Path filePath = Path.of(repositoryPath).resolve(argument);
        processPath(filePath);
    }

    private static void processPath(Path path) {
        if (Files.isDirectory(path) || String.valueOf(path).contains(".ngit") || String.valueOf(path).contains(".git")) {
            return;
        }

        FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(path);

        String gitObjectHash;

        try {
            gitObjectHash = addBlob(String.valueOf(ngitPath), String.valueOf(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BlobStatus blobStatus = new BlobStatus(path.getFileName().toString(), gitObjectHash, lastModifiedTime.toString());
        existingData.put(path.toString(), blobStatus);
    }

    static String addBlob(String ngitPath, String path) throws IOException {
        String shaOfFile = SHA.fileToSHA(path);
        String gitObjectDirectory = shaOfFile.substring(0, 2);
        String gitObjectName = shaOfFile.substring(2);

        String objectPath = ngitPath + "\\objects\\" + gitObjectDirectory;

        byte[] fileContents = Files.readAllBytes(Paths.get(path));
        byte[] compressedContents = compress(fileContents);

        NgitApplication.makeFile(objectPath, gitObjectName, "");

        String fullPath = objectPath + "\\" + gitObjectName;
        Files.write(Paths.get(fullPath), compressedContents, StandardOpenOption.CREATE);

        return shaOfFile;
    }

    private static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        try (java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream(data.length)) {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }
    }

    static Map<String, BlobStatus> readExistingData(Path filePath) {
        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (Map<String, BlobStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to read existing data", e);
        }
    }

    static void saveDataToFile(Path filePath, Map<String, BlobStatus> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
