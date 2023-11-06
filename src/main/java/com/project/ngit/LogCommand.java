package com.project.ngit;

import static com.project.ngit.CommitMaker.loadCommitStatus;

public class LogCommand {
    static String objectsPath;
    public static void execute(String repositoryPath) {
        objectsPath = repositoryPath + "\\.ngit\\objects";
        String directoryPath = repositoryPath + "\\.ngit\\heads";
        String currentBranch = SHA.getStringFromFile(directoryPath + "\\HEAD");
        String currentCommitSHA = SHA.getStringFromFile(directoryPath + "\\" + currentBranch);
        recursiveLog(currentCommitSHA);
    }

    private static void recursiveLog(String commitSHA) {
        if (commitSHA == null || commitSHA.isEmpty()) {
            return;
        }
        var commitContents = loadCommitStatus(objectsPath + "\\" + commitSHA.substring(0,2) + "\\" + commitSHA.substring(2));
        System.err.println(commitContents.content());
        System.out.println(commitContents.message());
        System.out.println(commitContents.creator());
        System.out.println();

        if (commitContents.previousCommit() != null) {
            recursiveLog(commitContents.previousCommit());
        }
    }
}
