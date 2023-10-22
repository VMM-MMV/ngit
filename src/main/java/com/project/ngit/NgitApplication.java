package com.project.ngit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.Scanner;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class NgitApplication {
	static boolean isRunning = true;

	static String GLOBAL_REPOSITORY_NAME = "/home/miguel/Desktop/Test";

	public static void main(String[] args) {
		NgitApplication gitClone = new NgitApplication();
		gitClone.cliLogic();
	}

	private void cliLogic() {
		try (Scanner scanner = new Scanner(System.in)) {

			while (isRunning) {
				String input = scanner.nextLine();
				processCommand(input);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void processCommand(String input) {
		if (input.startsWith("ngit ")) {
			String[] tokens = input.split(" ");

			String command = tokens[1];
			String argument = (tokens.length > 2) ? tokens[2] : null;


			switch (command) {
				case "init" -> initCommand();
				case "add" -> addCommand(argument);
				default -> System.out.println("Unknown command");
			}

		} else {
			System.out.println("Invalid command.");
		}

	}

	private void addCommand(String argument) {
		if (argument == null) {
			System.out.println("No argument provided for add command.");
			return;
		}
		Path filePath = Path.of(GLOBAL_REPOSITORY_NAME + "/" + ".ngit");

		if (argument.equals(".")) {
			try (Stream<Path> stream = Files.walk(filePath)) {
				stream.forEach(path -> {
					FileTime lastModifiedTime = getLastModifiedTime(path);
					System.out.println(path + " last modified: " + lastModifiedTime);
				});
			} catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
			FileTime lastModifiedTime = getLastModifiedTime(Path.of(filePath + "/" + argument));
		}
	}

	private void initCommand() {
		makeFolder(".ngit");
		makeFolder(".ngit/objects");
		makeFolder(".ngit/index");
	}

	public static FileTime getLastModifiedTime(Path path) {
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			return attrs.lastModifiedTime();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected void makeFolder(String folderName) {
		Path dirPath = Paths.get(GLOBAL_REPOSITORY_NAME + "/" +folderName);

		if (directoryExists(dirPath)) {
			System.err.println("Already exists");
			return;
		}

		try {
			Files.createDirectory(dirPath);
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + e.getMessage());
		}
	}

	public boolean directoryExists(Path directory) {
		return Files.exists(directory) && Files.isDirectory(directory);
	}

}
