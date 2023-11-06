package com.project.ngit;

import java.nio.file.Path;

public class TEST {
    public static void main(String[] args) {
        Path ngitPath = Path.of("C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit");
        var existingData = NgitApplication.readExistingData(ngitPath.resolve("index/changes.ser"));
        for (var item : existingData.entrySet()) {
            System.out.println(item);
        }
    }
}
