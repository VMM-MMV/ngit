package com.project.ngit.Commands;

import com.project.ngit.Commands.Commit.CommitMaker;
import com.project.ngit.Hash.SHA;
import com.project.ngit.NgitApplication;
import com.project.ngit.ObjectStatuses.TreeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * The CheckoutCommand class is responsible for checking out a branch or commit in an NGit repository.
 * This involves updating the HEAD to the specified state and resetting the working directory to match
 * the tree structure of the commit.
 */
public class CheckoutCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutCommand.class);
    private final Path ngitPath;
    private final Path repositoryPath;

    /**
     * Constructs a CheckoutCommand for a given repository path.
     *
     * @param repositoryPath the path to the repository where the checkout will be performed
     */
    public CheckoutCommand(String repositoryPath) {
        this.repositoryPath = Paths.get(repositoryPath);
        this.ngitPath = this.repositoryPath.resolve(".ngit");
    }

    /**
     * Executes the checkout operation using the provided hash.
     * The hash can represent either a branch name or a commit hash.
     *
     * @param hash the hash of the commit or the name of the branch to check out
     */
    public void execute(String hash) {
        Path headsPath = ngitPath.resolve("heads");

        try {
            if (fileExists(headsPath, hash)) {
                NgitApplication.makeFile(headsPath.toString(), "HEAD", hash);
                String shaOfCommit = SHA.getStringFromFile(headsPath.resolve(hash).toString());
                System.out.println(shaOfCommit);
                var commitInfo = CommitMaker.loadCommitStatus(Path.of(String.valueOf(ngitPath), "objects", shaOfCommit.substring(0, 2), shaOfCommit.substring(2)));
                createFoldersRecursively(commitInfo.content(), repositoryPath.toFile());
            } else {
                createFoldersRecursively(hash, repositoryPath.toFile());
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error while executing checkout command", e);
        }
    }

    /**
     * Checks if a file with the specified name exists in the given directory.
     *
     * @param directory the directory to check in
     * @param fileName  the name of the file to check for
     * @return true if the file exists and is a regular file, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean fileExists(Path directory, String fileName) throws IOException {
        try (Stream<Path> pathStream = Files.list(directory)) {
            return pathStream
                    .anyMatch(path -> path.getFileName().toString().equals(fileName) && Files.isRegularFile(path));
        }
    }

    /**
     * Recursively recreates folders and files based on the tree structure represented by the given SHA hash.
     *
     * @param shaOfDirectoryContents the SHA hash of the directory contents
     * @param parentDirectory       the directory within which to create the structure
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    private void createFoldersRecursively(String shaOfDirectoryContents, File parentDirectory) throws IOException, ClassNotFoundException {
        List<TreeStatus> treeStatuses = readTreeGitObject(shaOfDirectoryContents);
        if (treeStatuses == null) {
            return;
        }

        for (TreeStatus status : treeStatuses) {
            Path childPath = parentDirectory.toPath().resolve(status.name());
            if ("tree".equals(status.objectType())) {
                Files.createDirectories(childPath);
                createFoldersRecursively(status.hash(), childPath.toFile());
            } else if ("blob".equals(status.objectType())) {
                createFileFromBlob(parentDirectory.toPath(), status.name(), status.hash());
            }
        }
    }

    /**
     * Reads the tree git object from the NGit object store using the specified SHA hash.
     *
     * @param shaOfDirectoryContents the SHA hash of the directory contents
     * @return a list of TreeStatus objects representing the contents of the directory
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    private List<TreeStatus> readTreeGitObject(String shaOfDirectoryContents) throws IOException, ClassNotFoundException {
        Path objectPath = getGitObjectPath(shaOfDirectoryContents);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(objectPath))) {
            return (List<TreeStatus>) ois.readObject();
        }
    }

    /**
     * Creates a file from a blob in the NGit object store.
     *
     * @param parentDirectory   the directory to create the file in
     * @param name              the name of the file to create
     * @param shaOfBlobContents the SHA hash of the blob contents
     * @throws IOException if an I/O error occurs
     */
    private void createFileFromBlob(Path parentDirectory, String name, String shaOfBlobContents) throws IOException {
        Path objectPath = getGitObjectPath(shaOfBlobContents);
        byte[] compressedContents = readCompressedContents(objectPath);
        byte[] decompressedContents = decompressContents(compressedContents);
        Path outputFile = parentDirectory.resolve(name);
        writeToFile(decompressedContents, outputFile);
    }

    /**
     * Retrieves the path to a git object based on its SHA hash.
     *
     * @param shaOfBlobContents the SHA hash of the blob contents
     * @return the Path to the git object
     */
    private Path getGitObjectPath(String shaOfBlobContents) {
        return ngitPath.resolve("objects").resolve(shaOfBlobContents.substring(0, 2)).resolve(shaOfBlobContents.substring(2));
    }

    /**
     * Reads the compressed contents of a git object from the specified path.
     *
     * @param objectPath the path to the git object
     * @return a byte array containing the compressed contents of the object
     * @throws IOException if an I/O error occurs
     */
    private byte[] readCompressedContents(Path objectPath) throws IOException {
        return Files.readAllBytes(objectPath);
    }

    /**
     * Decompresses the contents of a git object.
     *
     * @param compressedContents the compressed contents of the object
     * @return a byte array containing the decompressed contents
     */
    private byte[] decompressContents(byte[] compressedContents) {
        Inflater inflater = new Inflater();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedContents);
             InflaterInputStream iis = new InflaterInputStream(bais, inflater);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = iis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress contents", e);
        } finally {
            inflater.end();
        }
    }

    /**
     * Writes the given byte array as the contents of a file at the specified path.
     *
     * @param decompressedContents the byte array to write to the file
     * @param outputFile           the path to the file to write
     * @throws IOException if an I/O error occurs
     */
    private void writeToFile(byte[] decompressedContents, Path outputFile) throws IOException {
        Files.write(outputFile, decompressedContents);
    }
}
