package com.project.ngit;

import static com.project.ngit.CommitCommand.loadCommitStatus;
import static com.project.ngit.CommitCommand.objectsPath;

public class LogCommand {
    public static void main(String[] args) {
        LogCommand logCommand = new LogCommand();
        logCommand.execute();
    }

    public void execute() {
        String directoryPath = "C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit\\heads";
        String currentBranch = SHA.getStringFromFile(directoryPath + "\\HEAD");
        String currentCommitSHA = SHA.getStringFromFile(directoryPath + "\\" + currentBranch);
        recursiveLog(currentCommitSHA);
    }

    private void recursiveLog(String commitSHA) {
        if (commitSHA == null || commitSHA.isEmpty()) {
            return;
        }
        var commitContents = loadCommitStatus(objectsPath + "\\" + commitSHA.substring(0,2) + "\\" + commitSHA.substring(2));
        System.out.println(commitContents.content());
        System.out.println(commitContents.message());
        System.out.println(commitContents.creator());
        System.out.println();

        if (commitContents.previousCommit() != null) {
            recursiveLog(commitContents.previousCommit());
        }
    }
}
