package com.project.ngit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class RecursiveAproach {

    private static final String NGIT_OBJECTS_PATH = "C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit\\objects\\";

    public static void main(String[] args) {
        // The SHA-1 provided will be the starting point to read the tree.
        createFilesRecursively("7d3d11a1e64b7b631cac0f140117dc0a13bedae20efc327839265a86a17621fb", new File("C:\\Users\\Miguel\\IdeaProjects\\ngit2\\output"));
    }

    public static void createFilesRecursively(String shaOfDirectoryContents, File parentDirectory) {
        List<TreeStatus> treeStatuses = readTree(shaOfDirectoryContents);
        if (treeStatuses == null) {
            // Exit if readTree returns null (deserialization error or file not found)
            return;
        }

        for (TreeStatus status : treeStatuses) {
            if ("tree".equals(status.objectType())) {
                File directory = new File(parentDirectory, status.name());
                if (!directory.exists() && !directory.mkdirs()) {
                    System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                }
                // Recurse into the directory
                createFilesRecursively(status.hash(), directory);
            } else if ("blob".equals(status.objectType())) {
                File blobFile = new File(parentDirectory, status.name());
                // Here we're assuming the blobs are stored by their hash as serialized byte arrays
                createBlob(String.valueOf(parentDirectory), status.name(), status.hash());
            }
        }
    }

    private static List<TreeStatus> readTree(String shaOfDirectoryContents) {
        String folderSHA = shaOfDirectoryContents.substring(0, 2);
        String fileSHA = shaOfDirectoryContents.substring(2);
        String filePath = NGIT_OBJECTS_PATH + folderSHA + "\\" + fileSHA;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            // Cast is unchecked, in practice, you'd want to add some kind of type checking here
            return (List<TreeStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createBlob(String parentDirectory, String name, String shaOfBlobContents) {
        String gitObjectDirectory = shaOfBlobContents.substring(0, 2);
        String gitObjectName = shaOfBlobContents.substring(2);
        String objectPath = NGIT_OBJECTS_PATH + gitObjectDirectory + "\\" + gitObjectName;

        // The output file path should be constructed with the parent directory and the name of the blob
        File outputFile = new File(parentDirectory, name); // Use the 'parentDirectory' from the calling context

        // Read the compressed file contents
        byte[] compressedContents = new byte[0];
        try {
            compressedContents = Files.readAllBytes(Paths.get(objectPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Decompress the contents
        Inflater inflater = new Inflater();
        try (InputStream is = new InflaterInputStream(new ByteArrayInputStream(compressedContents), inflater);
             OutputStream os = new FileOutputStream(outputFile)) { // Write to the output file
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Output file not found: " + outputFile.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while writing to output file: " + outputFile.getAbsolutePath(), e);
        } finally {
            inflater.end();
        }
    }

}
