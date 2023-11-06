package com.project.ngit;

import java.io.*;
import java.nio.file.Path;

public class CommitMaker {
    private final Path objectsPath;
    private final String headTree;
    private final Path ngitPath;

    public CommitMaker(Path objectsPath, String headTree, Path ngitPath) {
        this.objectsPath = objectsPath;
        this.headTree = headTree;
        this.ngitPath = ngitPath;
    }

    public void makeCommit(String commitMessage) {
        String directoryPath = ngitPath + "\\heads";
        try {
            if (isDirectoryEmpty(directoryPath)) {
                createFileInDirectory(directoryPath, "master", makeCommitBlob(null, commitMessage));
                createFileInDirectory(directoryPath, "HEAD", "master");
            } else {
                String currentBranch = SHA.getStringFromFile(directoryPath + "\\HEAD");
                String currentCommitSHA = SHA.getStringFromFile(directoryPath + "\\" + currentBranch);

                var commitContents = loadCommitStatus(objectsPath + "\\" + currentCommitSHA.substring(0,2) + "\\" + currentCommitSHA.substring(2));
                System.out.println(commitContents);
                String shaOfNewCommit = makeCommitBlob(commitContents.currentCommit(), commitMessage);
                createFileInDirectory(directoryPath, currentBranch, shaOfNewCommit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String makeCommitBlob(String pastCommitSHA, String commitMessage) {
        String commitSHA = SHA.computeSHA(headTree + pastCommitSHA + commitMessage + System.getProperty("user.name"));
        String gitObjectDirectory = commitSHA.substring(0, 2);
        String gitObjectName = commitSHA.substring(2);

        String gitObjectDir = objectsPath + "\\" + gitObjectDirectory;

        NgitApplication.makeFolder("", gitObjectDir);

        if (!commitSHA.equals(pastCommitSHA)) {
            saveCommitStatus(gitObjectDir, new CommitStatus(commitSHA, pastCommitSHA, System.getProperty("user.name"), headTree, commitMessage), gitObjectName);
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
}
