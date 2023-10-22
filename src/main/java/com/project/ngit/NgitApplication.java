package com.project.ngit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

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
		
		if (argument.equals(".")) {
			try (var stream = Files.walk(Path.of(GLOBAL_REPOSITORY_NAME + "/" + ".ngit"))) {
				stream.forEach(System.out::println);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			System.out.println(argument);
		}
	}



	private void initCommand() {
		makeFolder(".ngit");
		makeFolder(".ngit/objects");
		makeFolder(".ngit/index");
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
