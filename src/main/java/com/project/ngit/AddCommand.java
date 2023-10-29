package com.project.ngit;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;

public class AddCommand {
    static Path ngitPath;
    static String repositoryPath;
    static List<FileStatus> existingData = new ArrayList<>();

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
        if (Files.isDirectory(path)) {
            return;
        }

        FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(path);

        FileStatus fileStatus = new FileStatus(path.toString(), SHA.fileToSHA(String.valueOf(path)), lastModifiedTime.toString());
        existingData.add(fileStatus);
    }

    private static List<FileStatus> readExistingData(Path filePath) {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (List<FileStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to read existing data", e);
        }
    }

    private static void saveDataToFile(Path filePath, List<FileStatus> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}