package com.project.ngit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class RecursiveAproach {

    private static final String NGIT_OBJECTS_PATH = "C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit\\objects\\";

    public static void main(String[] args) {
        String startSHA = "8c5bb7e6ff4eed9a7f2df8e45a90e20184be67cc536d653facc70a708f28222e";
        createFilesRecursively(startSHA, new File("C:\\Users\\Miguel\\IdeaProjects\\ngit2\\output")); // Root directory to start creating files
    }

    public static void createFilesRecursively(String shaOfDirectoryContents, File parentDirectory) {
        List<TreeStatus> treeStatuses = readTree(shaOfDirectoryContents);
        if (treeStatuses == null) return;

        for (TreeStatus status : treeStatuses) {
            if ("tree".equals(status.objectType())) {
                File directory = new File(parentDirectory, status.name());
                if (!directory.exists() && !directory.mkdirs()) {
                    System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                }
                createFilesRecursively(status.hash(), directory);
            } else if ("blob".equals(status.objectType())) {
                File blobFile = new File(parentDirectory, status.name());
                try {
                    if (blobFile.createNewFile()) {
                        System.out.println("Blob created: " + blobFile.getAbsolutePath());
                    } else {
                        System.out.println("Blob already exists: " + blobFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<TreeStatus> readTree(String shaOfDirectoryContents) {
        String folderSHA = shaOfDirectoryContents.substring(0, 2);
        String fileSHA = shaOfDirectoryContents.substring(2);
        String filePath = NGIT_OBJECTS_PATH + folderSHA + "\\" + fileSHA;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<TreeStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}