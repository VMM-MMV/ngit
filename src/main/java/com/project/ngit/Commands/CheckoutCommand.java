package com.project.ngit.Commands;

import com.project.ngit.Commands.Commit.CommitMaker;
import com.project.ngit.NgitApplication;
import com.project.ngit.Hash.SHA;
import com.project.ngit.ObjectStatuses.TreeStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class CheckoutCommand {
    private final String ngitPath;
    private final String reporsitoryPath;

    public CheckoutCommand(String repositoryPath) {
        this.reporsitoryPath = repositoryPath;
        this.ngitPath = repositoryPath + "\\.ngit";
    }

    public void execute(String hash) {
        String headsPath = ngitPath + "\\heads";

        if (fileExists(headsPath, hash)) {
            NgitApplication.makeFile(headsPath, "HEAD", hash);
            String shaOfCommit = SHA.getStringFromFile(headsPath + "\\" + hash);
            var commitInfo = CommitMaker.loadCommitStatus(ngitPath + "\\objects\\" + shaOfCommit.substring(0,2) + "\\" + shaOfCommit.substring(2));
            createFoldersRecursively(commitInfo.content(), new File(reporsitoryPath));
        } else {
            createFoldersRecursively(hash, new File(ngitPath.substring(0, ngitPath.length() - 5)));
        }
    }

    public void createFoldersRecursively(String shaOfDirectoryContents, File parentDirectory) {
        List<TreeStatus> treeStatuses = readTree(shaOfDirectoryContents);
        if (treeStatuses == null) {
            return;
        }

        for (TreeStatus status : treeStatuses) {
            if ("tree".equals(status.objectType())) {
                File directory = new File(parentDirectory, status.name());
                createFoldersRecursively(status.hash(), directory);

            } else if ("blob".equals(status.objectType())) {
                createFileFromBlob(parentDirectory.getPath(), status.name(), status.hash());
            }
        }
    }

    public boolean fileExists(String repositoryPath, String fileName) {
        File repository = new File(repositoryPath);

        if (!repository.exists() || !repository.isDirectory()) {
            return false;
        }

        File[] listOfFiles = repository.listFiles();
        if (listOfFiles == null) {
            return false;
        }

        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().equals(fileName)) {
                return true;
            }
        }

        return false;
    }

    private List<TreeStatus> readTree(String shaOfDirectoryContents) {
        String filePath = getGitObjectPath(shaOfDirectoryContents);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<TreeStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createFileFromBlob(String parentDirectory, String name, String shaOfBlobContents) {
        String objectPath = getGitObjectPath(shaOfBlobContents);
        byte[] compressedContents = readCompressedContents(objectPath);
        byte[] decompressedContents = decompressContents(compressedContents);
        File outputFile = new File(parentDirectory, name);
        writeToFile(decompressedContents, outputFile);
    }

    public String getGitObjectPath(String shaOfBlobContents) {
        String gitObjectDirectory = shaOfBlobContents.substring(0, 2);
        String gitObjectName = shaOfBlobContents.substring(2);
        return ngitPath + "\\objects\\" + gitObjectDirectory + "\\" + gitObjectName;
    }

    private byte[] readCompressedContents(String objectPath) {
        try {
            return Files.readAllBytes(Paths.get(objectPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void writeToFile(byte[] decompressedContents, File outputFile) {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            os.write(decompressedContents);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Output file not found: " + outputFile.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while writing to output file: " + outputFile.getAbsolutePath(), e);
        }
    }
}
