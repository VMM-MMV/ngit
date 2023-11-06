package com.project.ngit;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.Deflater;

public class AddCommand {
    private Path ngitPath;
    private String repositoryPath;
    private Map<String, BlobStatus> existingData;

    public AddCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.ngitPath = Path.of(repositoryPath, ".ngit");
        this.existingData = new HashMap<>();
    }

    public void execute(String argument) {
        existingData = NgitApplication.readExistingData(ngitPath.resolve("index/changes.ser"));

        if (argument.equals(".")) {
            processAllFilesInRepository();
        } else {
            processSingleFile(argument);
        }

        NgitApplication.saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }

    private void processAllFilesInRepository() {
        try (Stream<Path> stream = Files.walk(Path.of(repositoryPath))) {
            stream.forEach(this::processPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processSingleFile(String argument) {
        Path filePath = Path.of(repositoryPath).resolve(argument);
        processPath(filePath);
    }

    private void processPath(Path path) {
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

    String addBlob(String ngitPath, String path) throws IOException {
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

    private byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }
    }
}
