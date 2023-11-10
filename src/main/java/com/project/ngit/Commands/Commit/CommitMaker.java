package com.project.ngit.Commands.Commit;

import com.project.ngit.NgitApplication;
import com.project.ngit.ObjectStatuses.CommitStatus;
import com.project.ngit.Hash.SHA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

public class CommitMaker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitMaker.class);

    private final Path objectsPath;
    private final String headTree;
    private final Path ngitPath;

    public CommitMaker(Path objectsPath, String headTree, Path ngitPath) {
        this.objectsPath = objectsPath;
        this.headTree = headTree;
        this.ngitPath = ngitPath;
    }

    public void makeCommit(String commitMessage) {
        Path directoryPath = ngitPath.resolve("heads");
        try {
            if (Files.isDirectory(directoryPath) && isDirectoryEmpty(directoryPath)) {
                createInitialBranchWithCommit(directoryPath, commitMessage);
            } else {
                makeNewCommitOnExistingBranch(directoryPath, commitMessage);
            }
        } catch (IOException e) {
            LOGGER.error("An I/O error occurred while making a commit", e);
        }
    }
    private void createInitialBranchWithCommit(Path directoryPath, String commitMessage) throws IOException {
        createFileInDirectory(directoryPath, "master", makeCommitBlob(null, commitMessage));
        createFileInDirectory(directoryPath, "HEAD", "master");
    }

    private void makeNewCommitOnExistingBranch(Path directoryPath, String commitMessage) throws IOException {
        Path headPath = directoryPath.resolve("HEAD");
        String currentBranch = new String(Files.readAllBytes(headPath));
        Path currentBranchPath = directoryPath.resolve(currentBranch);
        String currentCommitSHA = new String(Files.readAllBytes(currentBranchPath));

        Path commitPath = objectsPath.resolve(currentCommitSHA.substring(0, 2)).resolve(currentCommitSHA.substring(2));
        CommitStatus commitContents = loadCommitStatus(commitPath);
        String shaOfNewCommit = makeCommitBlob(commitContents.currentCommit(), commitMessage);
        createFileInDirectory(directoryPath, currentBranch, shaOfNewCommit);
    }

    private String makeCommitBlob(String pastCommitSHA, String commitMessage) throws IOException {
        String commitSHA = SHA.computeSHA(headTree);
        Path gitObjectDir = objectsPath.resolve(commitSHA.substring(0, 2));
        String gitObjectName = commitSHA.substring(2);

        NgitApplication.makeFolder("", gitObjectDir.toString());

        if (!commitSHA.equals(pastCommitSHA)) {
            Path filePath = gitObjectDir.resolve(gitObjectName);
            saveCommitStatus(filePath, new CommitStatus(commitSHA, pastCommitSHA, System.getProperty("user.name"), headTree, commitMessage));
        }
        return commitSHA;
    }

    public static void saveCommitStatus(Path path, CommitStatus status) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(status);
        }
    }

    public static CommitStatus loadCommitStatus(Path path) {
        CommitStatus status = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            status = (CommitStatus) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("An error occurred while loading commit status", e);
        }
        return status;
    }

    public static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    public static void createFileInDirectory(Path directoryPath, String fileName, String content) throws IOException {
        Path filePath = directoryPath.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(content);
        }
    }
}
