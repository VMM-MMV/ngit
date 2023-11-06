package com.project.ngit.Commands.Commit;

import com.project.ngit.ObjectStatuses.BlobStatus;
import com.project.ngit.Hash.SHA;
import com.project.ngit.ObjectStatuses.TreeStatus;

import java.io.*;
import java.nio.file.*;
import java.util.*;

class TreeMaker {
    private final Path objectsPath;
    private String headTree;
    private final Map<String, BlobStatus> existingData;

    public TreeMaker(Path objectsPath, Map<String, BlobStatus> existingData) {
        this.objectsPath = objectsPath;
        this.existingData = existingData;
    }

    public String getHeadTree() {
        return headTree;
    }

    public void makeTrees(Path ngitPath) {
        List<File> directories = listOfDirectories(ngitPath.getParent().toString());
        Collections.reverse(directories);

        for (File directory : directories) {
            makeOneTree(directory);
        }

        System.out.println(headTree);
    }

    private List<File> listOfDirectories(String rootDirectoryPath) {
        File rootDirectory = new File(rootDirectoryPath);
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("The provided file object is not a directory");
        }

        return breadthFirstDirectoryTraversal(rootDirectory);
    }

    private List<File> breadthFirstDirectoryTraversal(File rootDirectory) {
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

    private void makeOneTree(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        List<TreeStatus> treeInfo = collectTreeInfo(files);

        if (!treeInfo.isEmpty()) {
            String shaOfDirectoryContents = computeDirectorySHA(treeInfo);
            headTree = shaOfDirectoryContents; // It's value will change constantly and the last value here will be the head of all the trees

            writeTree(shaOfDirectoryContents, treeInfo);
            existingData.put(directory.getAbsolutePath(), new BlobStatus(directory.getName(), shaOfDirectoryContents, "tree"));
        }
    }

    private List<TreeStatus> collectTreeInfo(File[] files) {
        List<TreeStatus> treeInfo = new ArrayList<>();
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (existingData.containsKey(filePath)) {
                BlobStatus fileStatus = existingData.get(filePath);
                String objectType = file.isDirectory() ? "tree" : "blob";
                treeInfo.add(new TreeStatus(fileStatus.name(), fileStatus.fileHash(), objectType));
            }
        }
        return treeInfo;
    }

    private String computeDirectorySHA(List<TreeStatus> treeInfo) {
        StringBuilder directoryContentsHash = new StringBuilder();
        for (TreeStatus status : treeInfo) {
            directoryContentsHash.append(status.hash());
        }
        return SHA.computeSHA(directoryContentsHash.toString());
    }

    protected void writeTree(String shaOfDirectoryContents, List<TreeStatus> treeInfo) {
        String folderSHA = shaOfDirectoryContents.substring(0, 2);
        String fileSHA = shaOfDirectoryContents.substring(2);

        String filePath = objectsPath.resolve(folderSHA).toString();
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
    }
}