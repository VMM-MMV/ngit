package com.project.ngit;

import com.project.ngit.TreeStatus;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenSerializedFile {
    public static void main(String[] args) {
        List<TreeStatus> treeStatuses =  readTree("114c4ca4613e5371edc78a1e03eb7c9d24f3e5a9ad57cc82ad6601d27b894659");
    }

    public static List<TreeStatus> readTree(String shaOfDirectoryContents) {
        String folderSHA = shaOfDirectoryContents.substring(0, 2);
        String fileSHA = shaOfDirectoryContents.substring(2);
        String filePath = "C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit\\objects\\" + folderSHA + "\\" + fileSHA;

        List<TreeStatus> treeStatuses = new ArrayList<>();
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            treeStatuses = (List<TreeStatus>) objectInputStream.readObject();

            for (TreeStatus status : treeStatuses) {
                System.out.println(status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return treeStatuses;
    }
}
