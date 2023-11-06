package com.project.ngit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RebaseCommand {
    private final String headPath;

    public RebaseCommand(String repositoryPath) {
        this.headPath = repositoryPath + "\\.ngit\\heads\\";
    }

    public void execute(String fromDirectory, String toDirectory) {
        Path sourcePath = Paths.get(headPath + fromDirectory);
        Path destinationPath = Paths.get(headPath + toDirectory);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully!");
        } catch (IOException e) {
            System.err.println("Failed to copy file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
