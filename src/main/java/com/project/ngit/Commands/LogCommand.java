package com.project.ngit.Commands;

import com.project.ngit.Commands.Commit.CommitMaker;
import com.project.ngit.ObjectStatuses.CommitStatus;
import com.project.ngit.Hash.SHA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

public class LogCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogCommand.class);

    private final Path objectsPath;
    private final Path directoryPath;

    public LogCommand(String repositoryPath) {
        this.objectsPath = Paths.get(repositoryPath, ".ngit", "objects");
        this.directoryPath = Paths.get(repositoryPath, ".ngit", "heads");
    }

    public void execute() {
        try {
            Path headPath = directoryPath.resolve("HEAD");
            String currentBranch = SHA.getStringFromFile(headPath.toString());
            Path currentBranchPath = directoryPath.resolve(currentBranch);
            String currentCommitSHA = SHA.getStringFromFile(currentBranchPath.toString());
            recursiveLog(currentCommitSHA);
        } catch (Exception e) {
            LOGGER.error("Error executing log command", e);
        }
    }

    private void recursiveLog(String commitSHA) {
        if (commitSHA == null || commitSHA.isEmpty()) {
            return;
        }

        try {
            Path commitPath = objectsPath.resolve(commitSHA.substring(0, 2)).resolve(commitSHA.substring(2));
            CommitStatus commitContents = CommitMaker.loadCommitStatus(Path.of(commitPath.toFile().getAbsolutePath()));

            System.out.println(commitContents.content());
            System.out.println(commitContents.message());
            System.out.println(commitContents.creator());
            System.out.println();

            if (commitContents.previousCommit() != null) {
                recursiveLog(commitContents.previousCommit());
            }
        } catch (Exception e) {
            LOGGER.error("Error during recursive log", e);
        }
    }
}
