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
        if (String.valueOf(path).contains(".ngit") || String.valueOf(path).contains(".git")) {
            return;
        }

        FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(path);

        String gitObjectPath = null;
        if (Files.isDirectory(path)) {

        } else {
            gitObjectPath =  addBlob(String.valueOf(path));
        }

        FileStatus fileStatus = new FileStatus(path.toString(), gitObjectPath , lastModifiedTime.toString());
        existingData.add(fileStatus);
    }

    protected static void writeToFile(String fileName, String filePath, List<String> lines) {
        Path absoluteFilePath = Path.of(filePath, fileName);
        try {
            Files.write(absoluteFilePath, lines, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            System.out.println("Data written to file: " + absoluteFilePath);
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file.");
            e.printStackTrace();
        }
    }

    private static String addBlob(String path) {
        String shaOfFile = SHA.fileToSHA(path);
        String folderSHA = shaOfFile.substring(0, 2);
        String fileSHA = shaOfFile.substring(2);

        String filePath = ngitPath + "\\objects" + "\\" + folderSHA;

        NgitApplication.makeFile(fileSHA, filePath);


        String fileContents = SHA.getStringFromFile(path);

        String compressedContents = Zlib.compress(fileContents);

        List<String> fileString = Collections.singletonList(compressedContents);

        writeToFile(fileSHA, filePath, fileString);
        return filePath;
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