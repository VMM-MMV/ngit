package com.project.ngit;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static com.project.ngit.AddCommand.readExistingData;

public class DirectoryLister {
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
        Path ngitPath = Path.of(repositoryPath, ".ngit");
        Map<String, FileStatus> existingData = readExistingData(ngitPath.resolve("index/changes.ser"));
        List<File> directories = listDirectories(repositoryPath);
        for (File directory : directories) {
            checkFiles(directory, existingData);
        }
        System.out.println(existingData);
    }

    public static void checkFiles(File directory, Map<String, FileStatus> existingData) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        StringBuilder directoryContentsHash = new StringBuilder();

        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (existingData.containsKey(filePath)) {
                FileStatus fileStatus = existingData.get(filePath);

                directoryContentsHash.append(fileStatus.fileHash());//TO DO change path to relative path not absolute
                System.out.println("File: " + filePath + " - Status: " + fileStatus.fileHash());
            }
        }

        if(directoryContentsHash.length() != 0) {
            System.out.println(directoryContentsHash);
            var s = SHA.computeSHA(String.valueOf(directoryContentsHash));
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
//        File rootDirectory = new File("C:\\Users\\Miguel\\IdeaProjects\\ngit2");
//        List<File> directories = listDirectories(rootDirectory);
//        Collections.reverse(directories);
//        System.out.println(directories);
        makeTrees("C:\\Users\\Mihai Vieru\\ngit2");
    }
}
