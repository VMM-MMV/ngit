package com.project.ngit.Commands.Commit;

import com.project.ngit.ObjectStatuses.CommitStatus;
import com.project.ngit.Hash.SHA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * The {@code CommitMaker} class is responsible for creating commit objects and updating branch references
 * during the commit process in the NGIT version control system.
 */
public class CommitMaker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitMaker.class);
    private final Path objectsPath;
    private final String headTree;
    private final Path ngitPath;

    /**
     * Creates a new instance of {@code CommitMaker} with specified paths and tree information.
     *
     * @param objectsPath The path to the objects directory in the NGIT repository.
     * @param headTree    The SHA-2 hash of the tree object that will be referenced by the new commit.
     * @param ngitPath    The path to the NGIT directory in the repository.
     */
    public CommitMaker(Path objectsPath, String headTree, Path ngitPath) {
        this.objectsPath = objectsPath;
        this.headTree = headTree;
        this.ngitPath = ngitPath;
    }

    /**
     * Makes a commit with the given message. It determines whether to create an initial commit or
     * to append a new commit to an existing branch.
     *
     * @param commitMessage The message describing the commit.
     */
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

    /**
     * Creates the initial branch and commit if the repository is new and has no commits yet.
     *
     * @param directoryPath The path to the directory where the branch file will be created.
     * @param commitMessage The commit message for the initial commit.
     * @throws IOException If an I/O error occurs.
     */
    private void createInitialBranchWithCommit(Path directoryPath, String commitMessage) throws IOException {
        createFileInDirectory(directoryPath, "master", makeCommitGitObject(null, commitMessage));
        createFileInDirectory(directoryPath, "HEAD", "master");
    }

    /**
     * Makes a new commit on an existing branch using the provided commit message.
     *
     * @param directoryPath The path to the directory containing branch references.
     * @param commitMessage The message for the new commit.
     * @throws IOException If an I/O error occurs.
     */
    private void makeNewCommitOnExistingBranch(Path directoryPath, String commitMessage) throws IOException {
        Path headPath = directoryPath.resolve("HEAD");
        String currentBranch = new String(Files.readAllBytes(headPath));
        Path currentBranchPath = directoryPath.resolve(currentBranch);
        String currentCommitSHA = new String(Files.readAllBytes(currentBranchPath));

        Path commitPath = objectsPath.resolve(currentCommitSHA.substring(0, 2)).resolve(currentCommitSHA.substring(2));
        CommitStatus commitContents = loadCommitStatus(commitPath);
        String shaOfNewCommit = makeCommitGitObject(commitContents.currentCommit(), commitMessage);
        createFileInDirectory(directoryPath, currentBranch, shaOfNewCommit);
    }

    /**
     * Creates a commit git object in the NGIT objects directory with the given past commit SHA and commit message.
     *
     * @param pastCommitSHA The SHA-1 hash of the previous commit.
     * @param commitMessage The message for the commit.
     * @return The SHA-1 hash of the newly created commit.
     * @throws IOException If an I/O error occurs while creating the commit blob.
     */
    private String makeCommitGitObject(String pastCommitSHA, String commitMessage) throws IOException {
        String commitSHA = SHA.computeSHA(headTree + commitMessage + (pastCommitSHA != null ? pastCommitSHA : ""));
        Path gitObjectDir = objectsPath.resolve(commitSHA.substring(0, 2));
        Files.createDirectories(gitObjectDir); // Ensure the directory exists
        Path filePath = gitObjectDir.resolve(commitSHA.substring(2));

        if (!commitSHA.equals(pastCommitSHA)) {
            saveCommitStatus(filePath, new CommitStatus(commitSHA, pastCommitSHA, System.getProperty("user.name"), headTree, commitMessage));
        }
        return commitSHA;
    }

    /**
     * Saves the commit status to a file.
     *
     * @param path   The path to the file where the commit status will be saved.
     * @param status The commit status to save.
     * @throws IOException If an I/O error occurs while writing the commit status.
     */
    public static void saveCommitStatus(Path path, CommitStatus status) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(status);
        }
    }

    /**
     * Loads the commit status from a file.
     *
     * @param path The path to the file from which to load the commit status.
     * @return The loaded commit status.
     */
    public static CommitStatus loadCommitStatus(Path path) {
        CommitStatus status = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            status = (CommitStatus) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("An error occurred while loading commit status", e);
        }
        return status;
    }

    /**
     * Checks if a directory is empty.
     *
     * @param directory The path to the directory to check.
     * @return {@code true} if the directory is empty, {@code false} otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * Creates a file with the specified content in the given directory.
     *
     * @param directoryPath The path to the directory where the file will be created.
     * @param fileName      The name of the file to create.
     * @param content       The content to write to the file.
     * @throws IOException If an I/O error occurs.
     */
    public static void createFileInDirectory(Path directoryPath, String fileName, String content) throws IOException {
        Path filePath = directoryPath.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(content);
        }
    }
}
