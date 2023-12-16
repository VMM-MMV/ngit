package com.project.ngit.Commands;

import com.project.ngit.Commands.Commit.CommitMaker;
import com.project.ngit.ObjectStatuses.CommitStatus;
import com.project.ngit.Hash.SHA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;

/**
 * The LogCommand class is responsible for displaying the commit logs for the current branch.
 */
public class LogCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogCommand.class);

    private final Path objectsPath;
    private final Path directoryPath;

    /**
     * Constructs a LogCommand for the given repository path.
     *
     * @param repositoryPath the path to the repository for which the log will be displayed
     */
    public LogCommand(String repositoryPath) {
        this.objectsPath = Paths.get(repositoryPath, ".ngit", "objects");
        this.directoryPath = Paths.get(repositoryPath, ".ngit", "heads");
    }

    /**
     * Executes the log command, which prints the commit history of the current branch.
     */
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

    /**
     * Recursively prints the commit log starting from the specified commit SHA.
     * It prints the commit message, author, and other relevant information.
     *
     * @param commitSHA the SHA of the commit to start the log from
     */
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

            // Recursively log previous commits if they exist
            if (commitContents.previousCommit() != null) {
                recursiveLog(commitContents.previousCommit());
            }
        } catch (Exception e) {
            LOGGER.error("Error during recursive log", e);
        }
    }
}
