package com.project.ngit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.project.ngit.AddCommand.readExistingData;

public class DirectoryLister {
    static Path ngitPath;
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
        DirectoryLister.ngitPath = Path.of(repositoryPath, ".ngit");

        Map<String, FileStatus> existingData = readExistingData(ngitPath.resolve("index/changes.ser"));
        List<File> directories = listDirectories(repositoryPath);
        
        String lastTree = null;
        for (File directory : directories) {
            lastTree = makeOneTree(directory, existingData);
        }

        System.out.println(lastTree);
    }

    public static String makeOneTree(File directory, Map<String, FileStatus> existingData) {
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
                treeInfo.add(new TreeStatus(fileStatus.name(), fileStatus.fileHash()));

                directoryContentsHash.append(fileStatus.fileHash()); //TO DO change path to relative path not absolute
            }
        }

        if(directoryContentsHash.length() != 0) {
            System.out.println(directoryContentsHash);
            String shaOfDirectoryContents = SHA.computeSHA(String.valueOf(directoryContentsHash));
            System.out.println(shaOfDirectoryContents);
            addBlob(shaOfDirectoryContents, treeInfo);
            return shaOfDirectoryContents;
        }
        return null;
    }

    private static String addBlob(String shaOfDirectory, List<TreeStatus> statusOfTree) {
        String folderSHA = shaOfDirectory.substring(0, 2);
        String fileSHA = shaOfDirectory.substring(2);

        String filePath = ngitPath + "\\objects" + "\\" + folderSHA;

        NgitApplication.makeFile(fileSHA, filePath);

        List<String> fileString = Collections.singletonList(statusOfTree.toString());

        AddCommand.writeToFile(fileSHA, filePath, fileString);
        return filePath + "\\" + fileSHA;
    }


    public static void main(String[] args) {
        makeTrees("C:\\Users\\Mihai Vieru\\ngit2");
    }
}
