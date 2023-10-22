package com.project.ngit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

public class AddCommand {

    public static void execute(String repositoryPath, String argument) {
        if (argument == null) {
            System.out.println("No argument provided for add command.");
            return;
        }

        Path ngitPath = Path.of(repositoryPath + "/" + ".ngit");

        if (argument.equals(".")) {
            try (Stream<Path> stream = Files.walk(ngitPath)) {
                stream.forEach(path -> {
                    FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(path);
                    System.out.println(path + " last modified: " + lastModifiedTime);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            Path filePath = Path.of(ngitPath + "/" + argument);
            FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(filePath);
            System.out.println(ngitPath + " last modified: " + lastModifiedTime);
        }
    }
}
