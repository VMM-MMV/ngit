package com.project.ngit;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import static com.project.ngit.NgitApplication.*;

public class CommitCommand {
    static Path objectsPath;
    static Path ngitPath;
    static Map<String, BlobStatus> existingData;

    public static void execute(String repositoryPath, String commitMessage) {
        ngitPath = Path.of(repositoryPath + "\\.ngit\\");
        objectsPath = ngitPath.resolve("objects");
        existingData = NgitApplication.readExistingData(ngitPath.resolve("index/changes.ser"));
        CommitCommand.updateChangedFiles();

        TreeMaker treeMaker = new TreeMaker(objectsPath, existingData);
        treeMaker.makeTrees(ngitPath);

        CommitMaker commitMaker = new CommitMaker(objectsPath, treeMaker.getHeadTree(), ngitPath);
        commitMaker.makeCommit(commitMessage);
    }

    public static void updateChangedFiles() {
        // Iterate over the entries of the existing data map
        for (Map.Entry<String, BlobStatus> entry : new HashMap<>(existingData).entrySet()) {
            String filePath = entry.getKey();
            BlobStatus storedStatus = entry.getValue();

            try {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    continue;
                }

                FileTime currentModifiedTime = Files.getLastModifiedTime(path);

                // Check if the file has been modified since the last update
                if (!storedStatus.lastModifiedDate().equals(currentModifiedTime.toString())) {
                    // Rehash the file, save the blob, and update the existing data
                    String gitObjectHash = AddCommand.addBlob(String.valueOf(ngitPath), filePath);
                    BlobStatus updatedStatus = new BlobStatus(path.getFileName().toString(), gitObjectHash, currentModifiedTime.toString());
                    existingData.put(filePath, updatedStatus);
                }
            } catch (IOException e) {
                System.err.println("Error processing file: " + filePath);
                e.printStackTrace();
            }
        }

        saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }
}
