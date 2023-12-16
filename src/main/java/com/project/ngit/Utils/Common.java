package com.project.ngit.Utils;

import com.project.ngit.NgitApplication;
import com.project.ngit.ObjectStatuses.BlobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

public class Common {
    private static final Logger LOGGER = LoggerFactory.getLogger(Common.class);

    /**
     * Creates a folder at the specified path within the repository.
     * @param folderName The name of the folder to create.
     * @param repositoryPath The path to the repository where the folder will be created.
     */
    public static void makeFolder(String folderName, String repositoryPath) {
        Path dirPath = Paths.get(repositoryPath, folderName);

        if (directoryExists(dirPath)) {
            return;
        }

        try {
            Files.createDirectory(dirPath);
        } catch (IOException e) {
            LOGGER.error("Failed to create directory: {}", dirPath, e);
        }
    }

    /**
     * Retrieves the last modified time of the file at the given path.
     * @param path The path of the file.
     * @return The last modified time as a FileTime object.
     */
    public static FileTime getLastModifiedTime(Path path) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.lastModifiedTime();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates a file with the specified content at the given path within the repository.
     * @param filePath The path where the file will be created.
     * @param fileName The name of the file to create.
     * @param content The content to write to the file.
     */
    public static void makeFile(String filePath, String fileName, String content) {
        Path parentDirPath = Paths.get(filePath);
        Path absoluteFilePath = parentDirPath.resolve(fileName);

        try {
            // Create the directories if they do not exist
            Files.createDirectories(parentDirPath);

            // Write the content to the file, overwriting existing content
            Files.writeString(absoluteFilePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            LOGGER.error("Failed to write to file: {}", absoluteFilePath, e);
        }
    }

    /**
     * Checks if a directory exists at the given path.
     * @param directory The path of the directory to check.
     * @return True if the directory exists, false otherwise.
     */
    public static boolean directoryExists(Path directory) {
        return Files.exists(directory) && Files.isDirectory(directory);
    }

    /**
     * Reads existing BlobStatus data from a file.
     * @param filePath The path to the file from which to read the data.
     * @return A map of file paths to BlobStatus objects.
     */
    public static Map<String, BlobStatus> readExistingData(Path filePath) {
        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (Map<String, BlobStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to read existing data", e);
        }
    }

    /**
     * Saves BlobStatus data to a file.
     * @param filePath The path to the file where the data will be saved.
     * @param data The map of file paths to BlobStatus objects to save.
     */
    public static void saveDataToFile(Path filePath, Map<String, BlobStatus> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
