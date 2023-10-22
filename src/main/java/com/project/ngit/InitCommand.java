package com.project.ngit;

import static com.project.ngit.NgitApplication.makeFolder;

public class InitCommand {

    public static void execute(String repositoryPath) {
        makeFolder(repositoryPath, ".ngit");
        makeFolder(repositoryPath, ".ngit/objects");
        makeFolder(repositoryPath, ".ngit/index");
    }
}
