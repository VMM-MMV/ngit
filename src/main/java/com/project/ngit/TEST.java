package com.project.ngit;

import java.nio.file.Path;

public class TEST {
    public static void main(String[] args) {
        Path ngitPath = Path.of("C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit");
        var existingData = AddCommand.readExistingData(ngitPath.resolve("index/changes.ser"));
        System.out.println(existingData);
    }
}
