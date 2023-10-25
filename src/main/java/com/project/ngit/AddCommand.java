package com.project.ngit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class AddCommand {

    static Path ngitPath;

    public static void execute(String repositoryPath, String argument) {
        if (argument == null) {
            System.out.println("No argument provided for add command.");
            return;
        }

        ngitPath = Path.of(repositoryPath, ".ngit");

        Map<String, String> existingData = readExistingJsonData(ngitPath.resolve("index/changes.json"));

        if (argument.equals(".")) {
            try (Stream<Path> stream = Files.walk(Path.of(repositoryPath))) {
                stream.forEach(path -> {
                    FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(path);
                    System.out.println(path + " last modified: " + lastModifiedTime);
                    existingData.put(path.toString(), lastModifiedTime.toString());
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Path filePath = ngitPath.resolve(argument);
            FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(filePath);
            System.out.println(ngitPath + " last modified: " + lastModifiedTime);
            existingData.put(filePath.toString(), lastModifiedTime.toString());
        }
        saveToJsonFile(ngitPath.resolve("index/changes.json"), existingData);
    }

    private static Map<String, String> readExistingJsonData(Path filePath) {
        Map<String, String> data = new LinkedHashMap<>();
        if (!Files.exists(filePath)) {
            return data;
        }

        try {
            String content = new String(Files.readAllBytes(filePath));
            String[] pairs = content.replace("{", "").replace("}", "").split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    data.put(keyValue[0].trim().replace("\"", ""), keyValue[1].trim().replace("\"", ""));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read existing JSON data", e);
        }
        return data;
    }

    private static void saveToJsonFile(Path filePath, Map<String, String> data) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (!data.isEmpty()) {
            sb.setLength(sb.length() - 1);  // remove last comma
        }
        sb.append("}");

        try {
            Files.write(filePath, sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
