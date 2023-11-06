package com.project.ngit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static com.project.ngit.CommitCommand.loadCommitStatus;

public class CheckoutCommand {
    private static String NGIT_PATH;
    public static void execute(String repositoryPath, String hash) {
        NGIT_PATH = repositoryPath + "\\.ngit";
        String HEADS_PATH = NGIT_PATH + "\\heads";

        if (fileExists(HEADS_PATH, hash)) {
            NgitApplication.makeFile(HEADS_PATH, "HEAD", hash);
            String shaOfCommit = SHA.getStringFromFile(HEADS_PATH + "\\" + hash);
            var commitInfo = loadCommitStatus(NGIT_PATH + "\\objects\\" + shaOfCommit.substring(0,2) + "\\" + shaOfCommit.substring(2));
            createFilesRecursively(commitInfo.content(), new File(repositoryPath));
        } else {
            createFilesRecursively(hash, new File(repositoryPath));
        }
    }

    public static void createFilesRecursively(String shaOfDirectoryContents, File parentDirectory) {
        List<TreeStatus> treeStatuses = readTree(shaOfDirectoryContents);
        if (treeStatuses == null) {
            return;
        }

        for (TreeStatus status : treeStatuses) {
            if ("tree".equals(status.objectType())) {
                File directory = new File(parentDirectory, status.name());
                if (!directory.exists() && !directory.mkdirs()) {
                    System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                }
                createFilesRecursively(status.hash(), directory);

            } else if ("blob".equals(status.objectType())) {
                createBlob(String.valueOf(parentDirectory), status.name(), status.hash());
            }
        }
    }

    public static boolean fileExists(String repositoryPath, String fileName) {
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

    private static List<TreeStatus> readTree(String shaOfDirectoryContents) {
        String filePath = getGitObjectPath(shaOfDirectoryContents);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<TreeStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void createBlob(String parentDirectory, String name, String shaOfBlobContents) {
        String objectPath = getGitObjectPath(shaOfBlobContents);
        byte[] compressedContents = readCompressedContents(objectPath);
        byte[] decompressedContents = decompressContents(compressedContents);
        File outputFile = new File(parentDirectory, name);
        writeToFile(decompressedContents, outputFile);
    }

    public static String getGitObjectPath(String shaOfBlobContents) {
        String gitObjectDirectory = shaOfBlobContents.substring(0, 2);
        String gitObjectName = shaOfBlobContents.substring(2);
        return NGIT_PATH + "\\objects\\" + gitObjectDirectory + "\\" + gitObjectName;
    }

    private static byte[] readCompressedContents(String objectPath) {
        try {
            return Files.readAllBytes(Paths.get(objectPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] decompressContents(byte[] compressedContents) {
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

    private static void writeToFile(byte[] decompressedContents, File outputFile) {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            os.write(decompressedContents);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Output file not found: " + outputFile.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while writing to output file: " + outputFile.getAbsolutePath(), e);
        }
    }
}