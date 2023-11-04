package com.project.ngit;

import com.project.ngit.TreeStatus;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

public class OpenSerializedFile {
    public static void main(String[] args) {
        // Replace with your actual path to the serialized file, regardless of its extension
        String filePath = "C:\\Users\\Miguel\\IdeaProjects\\ngit2\\.ngit\\objects\\8c\\5bb7e6ff4eed9a7f2df8e45a90e20184be67cc536d653facc70a708f28222e";

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            // Cast the deserialized object to the correct type
            List<TreeStatus> treeStatuses = (List<TreeStatus>) objectInputStream.readObject();

            // Now you can work with the list of TreeStatus objects
            for (TreeStatus status : treeStatuses) {
                System.out.println(status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
