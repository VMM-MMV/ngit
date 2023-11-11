package com.project.ngit.Commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

public class RebaseCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(RebaseCommand.class);
    private final Path headPath;

    public RebaseCommand(String repositoryPath) {
        this.headPath = Paths.get(repositoryPath, ".ngit", "heads");
    }

    public void execute(String fromBranch, String toBranch) {
        Path sourcePath = headPath.resolve(fromBranch);
        Path destinationPath = headPath.resolve(toBranch);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Rebase successful");
        } catch (IOException e) {
            LOGGER.error("Failed to copy file: {}", e.getMessage(), e);
        }
    }
}
