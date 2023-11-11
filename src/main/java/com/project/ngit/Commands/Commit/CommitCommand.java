package com.project.ngit.Commands.Commit;

import com.project.ngit.Commands.AddCommand;
import com.project.ngit.NgitApplication;
import com.project.ngit.ObjectStatuses.BlobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class CommitCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitCommand.class);
    private final Path objectsPath;
    private final Path ngitPath;
    private final Map<String, BlobStatus> existingData;
    private final String repositoryPath;

    public CommitCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.ngitPath = Paths.get(repositoryPath, ".ngit");
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
        for (Map.Entry<String, BlobStatus> entry : new HashMap<>(existingData).entrySet()) {
            String filePath = entry.getKey();
            BlobStatus storedStatus = entry.getValue();

            try {
                Path path = Paths.get(filePath);
                if (!Files.exists(path)) {
                    existingData.remove(filePath);
                    continue;
                }

                FileTime currentModifiedTime = Files.getLastModifiedTime(path);

                if (!storedStatus.lastModifiedDate().equals(currentModifiedTime.toString())) {
                    String gitObjectHash = new AddCommand(repositoryPath).addBlob(ngitPath.toString(), filePath);
                    BlobStatus updatedStatus = new BlobStatus(path.getFileName().toString(), gitObjectHash, currentModifiedTime.toString());
                    existingData.put(filePath, updatedStatus);
                }
            } catch (IOException e) {
                LOGGER.error("Error processing file: {}", filePath, e);
            }
        }

        NgitApplication.saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }

}
