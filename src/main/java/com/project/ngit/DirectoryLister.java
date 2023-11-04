package com.project.ngit;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import static com.project.ngit.AddCommand.readExistingData;

public class DirectoryLister {
    static Path ngitPath;
    static Map<String, FileStatus> existingData;

    public static List<File> listDirectories(String rootDirectoryPath) {
        File rootDirectory = new File(rootDirectoryPath);
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("The provided file object is not a directory");
        }

        Queue<File> queue = new LinkedList<>();
        queue.offer(rootDirectory);

        List<File> directories = new ArrayList<>();
        directories.add(rootDirectory);

        while (!queue.isEmpty()) {
            File currentDir = queue.poll();
            File[] files = currentDir.listFiles();

            if (files == null) {
                continue;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    directories.add(file);
                    queue.offer(file);
                }
            }
        }
        return directories;
    }

    public static void makeTrees(String repositoryPath) {
        ngitPath = Path.of(repositoryPath, ".ngit");
        existingData = readExistingData(ngitPath.resolve("index/changes.ser"));
        List<File> directories = listDirectories(repositoryPath);
        Collections.reverse(directories);

        String headTree = null;
        for (File directory : directories) {
            headTree =  makeOneTree(directory);
        }

        System.out.println(headTree);
    }

    public static String makeOneTree(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return null;
        }

        StringBuilder directoryContentsHash = new StringBuilder();
        List<TreeStatus> treeInfo = new ArrayList<>();

        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (existingData.containsKey(filePath)) {
                FileStatus fileStatus = existingData.get(filePath);
                System.out.println(fileStatus.name());
                String objectType = file.isDirectory() ? "tree" : "blob";
                treeInfo.add(new TreeStatus(fileStatus.name(), fileStatus.fileHash(), objectType));

                directoryContentsHash.append(fileStatus.fileHash());
            }
        }

        if (directoryContentsHash.length() != 0) {
            String shaOfDirectoryContents = SHA.computeSHA(directoryContentsHash.toString());
            writeTree(shaOfDirectoryContents, treeInfo);
            existingData.put(String.valueOf(directory), new FileStatus(directory.getName(),shaOfDirectoryContents,"sss"));
            return shaOfDirectoryContents;
        }

        return null;
    }

    protected static String writeTree(String shaOfDirectoryContents, List<TreeStatus> treeInfo) {
        String folderSHA = shaOfDirectoryContents.substring(0, 2);
        String fileSHA = shaOfDirectoryContents.substring(2);

        String filePath = ngitPath.resolve("objects").resolve(folderSHA).toString();
        File directory = new File(filePath);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + directory);
        }

        String fullFilePath = Paths.get(filePath, fileSHA).toString();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fullFilePath))) {
            oos.writeObject(treeInfo);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing the tree to the file: " + fullFilePath, e);
        }

        return fullFilePath;
    }

    public static void main(String[] args) {
        makeTrees("C:\\Users\\Miguel\\IdeaProjects\\ngit2");
    }
}
