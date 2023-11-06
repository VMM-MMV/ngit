package com.project.ngit.Commands;

import com.project.ngit.NgitApplication;

import java.nio.file.Path;

public class BranchCommand {
    private final String repositoryPath;

    public BranchCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public void execute(String nameOfBranch) {
        String ngitPath = repositoryPath + "\\.ngit";

        if (!NgitApplication.directoryExists(Path.of(ngitPath + "\\heads"))) return;

        NgitApplication.makeFile(ngitPath + "\\heads", nameOfBranch, "");
    }

}
