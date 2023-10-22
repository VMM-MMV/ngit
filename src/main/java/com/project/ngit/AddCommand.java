package com.project.ngit;

import javax.json.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.stream.Stream;

public class AddCommand {

    static Path ngitPath;

    public static void execute(String repositoryPath, String argument) {
        if (argument == null) {
            System.out.println("No argument provided for add command.");
            return;
        }

        ngitPath = Path.of(repositoryPath + "/" + ".ngit");

        JsonObject existingData = readExistingJsonData(ngitPath.resolve("index/changes.json"));

        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder(existingData);

        if (argument.equals(".")) {
            try (Stream<Path> stream = Files.walk(ngitPath)) {
                stream.forEach(path -> {
                    FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(path);
                    System.out.println(path + " last modified: " + lastModifiedTime);
                    jsonBuilder.add(path.toString(), lastModifiedTime.toString());
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            Path filePath = Path.of(ngitPath + "/" + argument);
            FileTime lastModifiedTime = NgitApplication.getLastModifiedTime(filePath);
            System.out.println(ngitPath + " last modified: " + lastModifiedTime);
            jsonBuilder.add(filePath.toString(), lastModifiedTime.toString());
        }
        saveToJsonFile(ngitPath.resolve("index/changes.json"), jsonBuilder.build());
    }

    private static JsonObject readExistingJsonData(Path filePath) {
        if (!Files.exists(filePath)) {
            return Json.createObjectBuilder().build(); // return an empty JSON object if the file doesn't exist
        }

        try {
            String content = new String(Files.readAllBytes(filePath));
            try (StringReader sr = new StringReader(content);
                 JsonReader reader = Json.createReader(sr)) {
                return reader.readObject();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read existing JSON data", e);
        }
    }

    private static void saveToJsonFile(Path filePath, JsonObject jsonContent) {
        try (StringWriter sw = new StringWriter();
             JsonWriter writer = Json.createWriter(sw)) {
             writer.writeObject(jsonContent);
             Files.write(filePath, sw.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
