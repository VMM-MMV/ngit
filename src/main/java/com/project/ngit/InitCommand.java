package com.project.ngit;

public class InitCommand {
    private String repositoryPath;

    public InitCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public void execute() {
        NgitApplication.makeFolder(".ngit", repositoryPath);
        NgitApplication.makeFolder(".ngit/objects", repositoryPath);
        NgitApplication.makeFolder(".ngit/index", repositoryPath);
        NgitApplication.makeFolder(".ngit/heads", repositoryPath);
    }
}
