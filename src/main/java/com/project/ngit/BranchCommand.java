package com.project.ngit;

import java.nio.file.Path;

public class BranchCommand {
    public static void execute(String repositoryPath, String nameOfBranch) {
        String ngitPath = repositoryPath + "\\.ngit";

        if (!NgitApplication.directoryExists(Path.of(ngitPath + "\\heads"))) {
            return;
        }

        NgitApplication.makeFile(ngitPath + "\\heads", nameOfBranch, "");
    }

    public static void main(String[] args) {
        BranchCommand.execute("C:\\Users\\Miguel\\Desktop\\NewFolder", "main");
    }
}
