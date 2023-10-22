package com.project.ngit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class NgitApplication {
	static boolean isRunning = true;

	static String globalRepositoryPath = "/home/miguel/Desktop/Test";

	public static void main(String[] args) {
		cliLogic();
	}

	private static void cliLogic() {
		try (Scanner scanner = new Scanner(System.in)) {

			while (isRunning) {
				String input = scanner.nextLine();
				switch (input) {
					case "ngit init" -> initCommand();
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void initCommand() {
		makeFolder(".ngit");
		makeFolder(".ngit/objects");
		makeFolder(".ngit/index");
	}

	protected static void makeFolder(String folderName) {
		Path dirPath = Paths.get(globalRepositoryPath + "/" +folderName);

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
