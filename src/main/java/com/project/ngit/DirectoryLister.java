package com.project.ngit;

import java.io.File;
import java.util.*;

public class DirectoryLister {
    public static List<File> listDirectories(File rootDirectory) {
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("The provided file object is not a directory");
        }

        Queue<File> queue = new LinkedList<>();
        queue.offer(rootDirectory);

        List<File> directories = new ArrayList<>();
        directories.add(rootDirectory);

        while (!queue.isEmpty()) {
            File currentDir = queue.poll();
            File[] files = currentDir.listFiles();

            if (files == null) {
                continue;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    directories.add(file);
                    queue.offer(file);
                }
            }
        }
        return directories;
    }

    public static void main(String[] args) {
        File rootDirectory = new File("C:\\Users\\Miguel\\IdeaProjects\\ngit2");
        List<File> directories = listDirectories(rootDirectory);
        Collections.reverse(directories);
        System.out.println(directories);
    }
}
