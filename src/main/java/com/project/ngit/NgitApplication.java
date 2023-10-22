package com.project.ngit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Scanner;
import java.nio.file.attribute.BasicFileAttributes;

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

			Runnable action = switch (command) {
				case "init" -> () -> InitCommand.execute(GLOBAL_REPOSITORY_NAME);
				case "add" -> () -> AddCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
				default -> () -> System.out.println("Unknown command");
			};

			action.run();
		} else {
			System.out.println("Invalid command.");
		}
	}

	public static FileTime getLastModifiedTime(Path path) {
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			return attrs.lastModifiedTime();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected static void makeFolder(String folderName, String s) {
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

	public static boolean directoryExists(Path directory) {
		return Files.exists(directory) && Files.isDirectory(directory);
	}

}
