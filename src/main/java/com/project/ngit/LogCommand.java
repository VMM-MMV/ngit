package com.project.ngit;

public class LogCommand {
    private final String objectsPath;
    private final String directoryPath;

    public LogCommand(String repositoryPath) {
        this.objectsPath = repositoryPath + "\\.ngit\\objects";
        this.directoryPath = repositoryPath + "\\.ngit\\heads";
    }

    public void execute() {
        String currentBranch = SHA.getStringFromFile(directoryPath + "\\HEAD");
        String currentCommitSHA = SHA.getStringFromFile(directoryPath + "\\" + currentBranch);
        recursiveLog(currentCommitSHA);
    }

    private void recursiveLog(String commitSHA) {
        if (commitSHA == null || commitSHA.isEmpty()) {
            return;
        }
        var commitContents = CommitMaker.loadCommitStatus(objectsPath + "\\" + commitSHA.substring(0,2) + "\\" + commitSHA.substring(2));
        System.err.println(commitContents.content());
        System.out.println(commitContents.message());
        System.out.println(commitContents.creator());
        System.out.println();

        if (commitContents.previousCommit() != null) {
            recursiveLog(commitContents.previousCommit());
        }
    }
}
