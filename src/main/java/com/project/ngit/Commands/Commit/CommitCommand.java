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

/**
 * The {@code CommitCommand} class encapsulates the functionality to handle the 'commit' command within the NGIT version control system.
 * It manages the process of updating the object database with changes that have been staged for commit and creating a new commit object
 * that points to the snapshot of the content at the time of commit.
 */
public class CommitCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitCommand.class);
    private final Path objectsPath;
    private final Path ngitPath;
    private final Map<String, BlobStatus> existingData;
    private final String repositoryPath;

    /**
     * Constructs a new {@code CommitCommand} using the given repository path.
     * It initializes the paths used by the NGIT system and reads the existing data(a file where files from "ngit add", function are added) from the index folder.
     *
     * @param repositoryPath the path to the repository where the commit will be performed, aka the place from where you run the command.
     */
    public CommitCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.ngitPath = Paths.get(repositoryPath, ".ngit");
        this.objectsPath = ngitPath.resolve("objects");
        this.existingData = NgitApplication.readExistingData(ngitPath.resolve("index/changes.ser"));
    }

    /**
     * Executes the commit process with the provided commit message.
     * It updates the changed files. Creates a tree structure representing the current index state,
     * and finally creates a commit object with the given message, and tree stored in it. In this way the tree can be accessed from the commit.
     *
     * @param commitMessage the message associated with the commit.
     */
    public void execute(String commitMessage) {
        this.updateChangedFiles();

        TreeMaker treeMaker = new TreeMaker(objectsPath, existingData);
        treeMaker.makeTrees(ngitPath);

        CommitMaker commitMaker = new CommitMaker(objectsPath, treeMaker.getHeadTree(), ngitPath);
        commitMaker.makeCommit(commitMessage);
    }

    /**
     * Updates the status of changed files in the index. It checks for file modifications since the last commit
     * and updates the index accordingly. If a file has been modified, it stages the new version of the file
     * for the next commit. Since otherwise the hash will be computed on the same info as the last files.
     */
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
