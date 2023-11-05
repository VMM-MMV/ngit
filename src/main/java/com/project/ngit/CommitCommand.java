package com.project.ngit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.io.*;
import java.nio.file.*;

import static com.project.ngit.AddCommand.*;

public class CommitCommand {
    static Path objectsPath;
    static String headTree;
    static String repositoryPath;
    static Map<String, FileStatus> existingData;

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
        String directoryPath = ngitPath + "\\heads";
        try {
            if (isDirectoryEmpty(directoryPath)) {
                createFileInDirectory(directoryPath, "master", makeCommitBlob(null));
                createFileInDirectory(directoryPath, "HEAD", "master");
            } else {
                String currentBranch = SHA.getStringFromFile(directoryPath + "\\HEAD");
                String currentCommitSHA = SHA.getStringFromFile(directoryPath + "\\" + currentBranch);

                var commitContents = loadCommitStatus(objectsPath + "\\" + currentCommitSHA.substring(0,2) + "\\" + currentCommitSHA.substring(2));
                System.out.println(commitContents);
                String shaOfNewCommit = makeCommitBlob(commitContents.currentCommit());
                createFileInDirectory(directoryPath, currentBranch, shaOfNewCommit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeCommitBlob(String pastObjectPath) {
        String commitSHA = SHA.computeSHA(headTree);
        String gitObjectDirectory = commitSHA.substring(0, 2);
        String gitObjectName = commitSHA.substring(2);

        String gitObjectDir = objectsPath + "\\" + gitObjectDirectory;

        NgitApplication.makeFolder("", gitObjectDir);

        if (!commitSHA.equals(pastObjectPath)) {
            saveCommitStatus(gitObjectDir, new CommitStatus(commitSHA, pastObjectPath, System.getProperty("user.name"), headTree,"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), gitObjectName);
        }
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

    public void updateChangedFiles() {
        // Iterate over the entries of the existing data map
        for (Map.Entry<String, FileStatus> entry : new HashMap<>(existingData).entrySet()) {
            String filePath = entry.getKey();
            FileStatus storedStatus = entry.getValue();

            try {
                Path path = Paths.get(filePath);
                // Skip if the file does not exist
                if (!Files.exists(path)) {
                    continue;
                }

                FileTime currentModifiedTime = Files.getLastModifiedTime(path);

                // Check if the file has been modified since the last update
                if (!storedStatus.lastModifiedDate().equals(currentModifiedTime.toString())) {
                    // Rehash the file, save the blob, and update the existing data
                    String gitObjectHash = addBlob(String.valueOf(ngitPath), filePath);
                    FileStatus updatedStatus = new FileStatus(path.getFileName().toString(), gitObjectHash, currentModifiedTime.toString());
                    existingData.put(filePath, updatedStatus);
                }
            } catch (IOException e) {
                System.err.println("Error processing file: " + filePath);
                e.printStackTrace();
            }
        }

        saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }


    public static void execute(String repositoryPath){
        CommitCommand.repositoryPath = repositoryPath;
        ngitPath = Path.of(repositoryPath + "\\.ngit\\");
        objectsPath = Path.of(ngitPath + "\\objects");
        existingData = readExistingData(ngitPath.resolve("index/changes.ser"));
        CommitCommand commitCommand = new CommitCommand();
        commitCommand.updateChangedFiles();
        commitCommand.makeTrees();
        commitCommand.makeCommit();
    }
}
