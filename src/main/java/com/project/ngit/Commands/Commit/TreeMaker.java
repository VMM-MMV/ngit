package com.project.ngit.Commands.Commit;

import com.project.ngit.Hash.SHA;
import com.project.ngit.ObjectStatuses.BlobStatus;
import com.project.ngit.ObjectStatuses.TreeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

class TreeMaker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeMaker.class);
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
        List<Path> directories = listOfDirectories(ngitPath.getParent());
        Collections.reverse(directories);

        for (Path directory : directories) {
            makeOneTree(directory);
        }

        System.out.println(headTree);
    }

    private List<Path> listOfDirectories(Path rootDirectoryPath) {
        if (!Files.isDirectory(rootDirectoryPath)) {
            throw new IllegalArgumentException("The provided path is not a directory");
        }

        try {
            return Files.walk(rootDirectoryPath)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error traversing directories", e);
        }
    }

    private void makeOneTree(Path directoryPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            List<TreeStatus> treeInfo = collectTreeInfo(stream);

            if (!treeInfo.isEmpty()) {
                String shaOfDirectoryContents = computeDirectorySHA(treeInfo);
                headTree = shaOfDirectoryContents; // It's value will change constantly and the last value here will be the head of all the trees

                writeTree(shaOfDirectoryContents, treeInfo);
                existingData.put(String.valueOf(directoryPath), new BlobStatus(directoryPath.getFileName().toString(), shaOfDirectoryContents, "tree"));
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while listing files in directory: {}", directoryPath, e);
        }
    }

    private List<TreeStatus> collectTreeInfo(DirectoryStream<Path> directoryStream) {
        List<TreeStatus> treeInfo = new ArrayList<>();
        for (Path path : directoryStream) {
            if (existingData.containsKey(path)) {
                BlobStatus fileStatus = existingData.get(path);
                String objectType = Files.isDirectory(path) ? "tree" : "blob";
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
        Path folderPath = objectsPath.resolve(shaOfDirectoryContents.substring(0, 2));
        Path treeFilePath = folderPath.resolve(shaOfDirectoryContents.substring(2));

        try {
            Files.createDirectories(folderPath);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(treeFilePath))) {
                oos.writeObject(treeInfo);
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing the tree to the file: {}", treeFilePath, e);
        }
    }
}
