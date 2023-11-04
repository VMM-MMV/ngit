package com.project.ngit;

import com.project.ngit.TreeStatus;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenSerializedFile {
    public static void main(String[] args) {
        List<TreeStatus> treeStatuses =  readTree("8c5bb7e6ff4eed9a7f2df8e45a90e20184be67cc536d653facc70a708f28222e");
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
