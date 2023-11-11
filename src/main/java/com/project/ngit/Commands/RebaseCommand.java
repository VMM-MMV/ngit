package com.project.ngit.Commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

/**
 * The RebaseCommand class is responsible for rebasing one branch onto another.
 * This is typically done to move a series of commits to a new base commit.
 */
public class RebaseCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RebaseCommand.class);
    private final Path headPath;

    /**
     * Constructs a RebaseCommand with the specified repository path.
     *
     * @param repositoryPath the path to the repository where the rebase will occur
     */
    public RebaseCommand(String repositoryPath) {
        this.headPath = Paths.get(repositoryPath, ".ngit", "heads");
    }

    /**
     * Executes the rebase operation by copying the reference from one branch to another.
     * This effectively moves the branch to the tip of another, changing the base of the branch's commit history.
     *
     * @param fromBranch the name of the branch to be rebased
     * @param toBranch   the name of the branch onto which the other branch will be rebased
     */
    public void execute(String fromBranch, String toBranch) {
        Path sourcePath = headPath.resolve(fromBranch);
        Path destinationPath = headPath.resolve(toBranch);
        try {
            // Perform the copy operation to rebase the branch
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Rebase successful");
        } catch (IOException e) {
            // Log the exception if the rebase fails
            LOGGER.error("Failed to copy file: {}", e.getMessage(), e);
        }
    }
}
