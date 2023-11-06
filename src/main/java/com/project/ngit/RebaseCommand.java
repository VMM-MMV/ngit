package com.project.ngit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class RebaseCommand {
    public static void execute(String repositoryPath, String fromDirectory, String toDirectory) {
        String HEAD_PATH = repositoryPath + "\\.ngit\\heads\\";
        String sourceFile = HEAD_PATH + fromDirectory;
        String destinationFile = HEAD_PATH + toDirectory;
        try {
            Files.copy(Paths.get(sourceFile), Paths.get(destinationFile), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
