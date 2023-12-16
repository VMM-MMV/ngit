package com.project.ngit.Test;

import com.project.ngit.NgitApplication;
import com.project.ngit.Utils.Common;

import java.nio.file.Path;

public class TEST {
    public static void main(String[] args) {
        Path ngitPath = Path.of("C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit");
        var existingData = Common.readExistingData(ngitPath.resolve("index/changes.ser"));
        for (var item : existingData.entrySet()) {
            System.out.println(item);
        }
    }
}
