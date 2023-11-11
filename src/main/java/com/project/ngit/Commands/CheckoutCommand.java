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
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class CheckoutCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutCommand.class);
    private final Path ngitPath;
    private final Path repositoryPath;

    public CheckoutCommand(String repositoryPath) {
        this.repositoryPath = Paths.get(repositoryPath);
        this.ngitPath = this.repositoryPath.resolve(".ngit");
    }

    public void execute(String hash) {
        Path headsPath = ngitPath.resolve("heads");

        try {
            if (fileExists(headsPath, hash)) {
                NgitApplication.makeFile(headsPath.toString(), "HEAD", hash);
                String shaOfCommit = SHA.getStringFromFile(headsPath.resolve(hash).toString());
                var commitInfo = CommitMaker.loadCommitStatus(Path.of(ngitPath.resolve("objects").resolve(shaOfCommit.substring(0, 2)).resolve(shaOfCommit.substring(2)).toString()));
                createFoldersRecursively(commitInfo.content(), repositoryPath.toFile());
            } else {
                createFoldersRecursively(hash, repositoryPath.toFile());
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error while executing checkout command", e);
        }
    }

    private boolean fileExists(Path directory, String fileName) throws IOException {
        return Files.list(directory)
                .anyMatch(path -> path.getFileName().toString().equals(fileName) && Files.isRegularFile(path));
    }

    private void createFoldersRecursively(String shaOfDirectoryContents, File parentDirectory) throws IOException, ClassNotFoundException {
        List<TreeStatus> treeStatuses = readTree(shaOfDirectoryContents);
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

    private List<TreeStatus> readTree(String shaOfDirectoryContents) throws IOException, ClassNotFoundException {
        Path objectPath = getGitObjectPath(shaOfDirectoryContents);
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(objectPath))) {
            return (List<TreeStatus>) ois.readObject();
        }
    }

    private void createFileFromBlob(Path parentDirectory, String name, String shaOfBlobContents) throws IOException {
        Path objectPath = getGitObjectPath(shaOfBlobContents);
        byte[] compressedContents = readCompressedContents(objectPath);
        byte[] decompressedContents = decompressContents(compressedContents);
        Path outputFile = parentDirectory.resolve(name);
        writeToFile(decompressedContents, outputFile);
    }

    private Path getGitObjectPath(String shaOfBlobContents) {
        return ngitPath.resolve("objects").resolve(shaOfBlobContents.substring(0, 2)).resolve(shaOfBlobContents.substring(2));
    }

    private byte[] readCompressedContents(Path objectPath) throws IOException {
        return Files.readAllBytes(objectPath);
    }

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

    private void writeToFile(byte[] decompressedContents, Path outputFile) throws IOException {
        Files.write(outputFile, decompressedContents);
    }
}
