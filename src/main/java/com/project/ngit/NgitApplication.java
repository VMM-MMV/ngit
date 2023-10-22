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
					case "ngit init" -> {
						System.out.println("aaa");
						makeFolder(".ngit");
						makeFolder(".ngit/objects");
						makeFolder(".ngit/index");
					}
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	protected static void makeFolder(String folderName) {
		Path dirPath = Paths.get(globalRepositoryPath + "/" +folderName);

		if (directoryExists(folderName)) {
			return;
		}

		try {
			Files.createDirectory(dirPath);
			System.out.println("Directory created successfully");
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + e.getMessage());
		}
	}

	public static boolean directoryExists(String dirPath) {
		Path directory = Paths.get(dirPath);
		return Files.exists(directory) && Files.isDirectory(directory);
	}

}
