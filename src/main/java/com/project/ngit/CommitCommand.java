package com.project.ngit;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class CommitCommand {
    static Path ngitPath = Path.of("C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit");
    static Path objectsPath = Path.of(ngitPath + "\\objects");
    static String headTree;
    static String repositoryPath = "C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit";
    static Map<String, FileStatus> existingData = AddCommand.readExistingData(ngitPath.resolve("index/changes.ser"));;

    public List<File> listOfDirectories(String rootDirectoryPath) {
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

    public void makeTrees() {
        List<File> directories = listOfDirectories(ngitPath.getParent().toString());
        Collections.reverse(directories);

        for (File directory : directories) {
            makeOneTree(directory);
        }

        System.out.println(headTree);
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
            existingData.put(directory.getAbsolutePath(), new FileStatus(directory.getName(), shaOfDirectoryContents, "tree"));
        }
    }

    private List<TreeStatus> collectTreeInfo(File[] files) {
        List<TreeStatus> treeInfo = new ArrayList<>();
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            if (existingData.containsKey(filePath)) {
                FileStatus fileStatus = existingData.get(filePath);
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


    protected static void writeTree(String shaOfDirectoryContents, List<TreeStatus> treeInfo) {
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

    private void makeCommit() {
        String directoryPath = repositoryPath + "\\heads";
        try {
            if (isDirectoryEmpty(directoryPath)) {
                createFileInDirectory(directoryPath, "master", makeCommitBlob());
                createFileInDirectory(directoryPath, "HEAD", "master");
            } else {
                System.out.println("Directory is not empty.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeCommitBlob() {
        String commitSHA = SHA.computeSHA(headTree);
        String gitObjectDirectory = commitSHA.substring(0, 2);
        String gitObjectName = commitSHA.substring(2);

        String gitObjectDir = objectsPath + "\\" + gitObjectDirectory;
        String gitObjectPath = gitObjectDir + "\\" + gitObjectName;

        NgitApplication.makeFolder("", gitObjectDir);

        saveCommitStatus(gitObjectDir, new CommitStatus(gitObjectPath, null, System.getProperty("user.name"), "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), gitObjectName);
        return commitSHA;
    }

    public static void saveCommitStatus(String path, CommitStatus status, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + "\\" + filename))) {
            oos.writeObject(status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static CommitStatus loadCommitStatus(String filename) {
        CommitStatus status = null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            status = (CommitStatus) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return status;
    }

    public static boolean isDirectoryEmpty(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length == 0;
        } else {
            System.out.println("The provided path is not a directory.");
            return false;
        }
    }

    public static void createFileInDirectory(String directoryPath, String fileName, String content) throws IOException {
        File file = new File(directoryPath, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    public static void main(String[] args) {
        CommitCommand commitCommand = new CommitCommand();
        commitCommand.makeTrees();
        commitCommand.makeCommit();
    }
}
