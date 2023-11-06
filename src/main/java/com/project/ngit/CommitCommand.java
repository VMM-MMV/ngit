package com.project.ngit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class CommitCommand {
    private final Path objectsPath;
    private final Path ngitPath;
    private final Map<String, BlobStatus> existingData;
    private final String repositoryPath;

    public CommitCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.ngitPath = Path.of(repositoryPath, ".ngit");
        this.objectsPath = ngitPath.resolve("objects");
        this.existingData = NgitApplication.readExistingData(ngitPath.resolve("index/changes.ser"));
    }

    public void execute(String commitMessage) {
        this.updateChangedFiles();

        TreeMaker treeMaker = new TreeMaker(objectsPath, existingData);
        treeMaker.makeTrees(ngitPath);

        CommitMaker commitMaker = new CommitMaker(objectsPath, treeMaker.getHeadTree(), ngitPath);
        commitMaker.makeCommit(commitMessage);
    }

    private void updateChangedFiles() {
        // Iterate over the entries of the existing data map
        for (Map.Entry<String, BlobStatus> entry : new HashMap<>(existingData).entrySet()) {
            String filePath = entry.getKey();
            BlobStatus storedStatus = entry.getValue();

            try {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    existingData.remove(filePath); // If file doesn't exist, remove it from the map
                    continue;
                }

                FileTime currentModifiedTime = Files.getLastModifiedTime(path);

                // Check if the file has been modified since the last update
                if (!storedStatus.lastModifiedDate().equals(currentModifiedTime.toString())) {
                    // Rehash the file, save the blob, and update the existing data
                    String gitObjectHash = new AddCommand(repositoryPath).addBlob(ngitPath.toString(), filePath);
                    BlobStatus updatedStatus = new BlobStatus(path.getFileName().toString(), gitObjectHash, currentModifiedTime.toString());
                    existingData.put(filePath, updatedStatus);
                }
            } catch (IOException e) {
                System.err.println("Error processing file: " + filePath);
                e.printStackTrace();
            }
        }

        NgitApplication.saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }
}
