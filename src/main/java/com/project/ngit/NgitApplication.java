package com.project.ngit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.nio.file.attribute.BasicFileAttributes;

public class NgitApplication {
	final static String GLOBAL_REPOSITORY_NAME = System.getProperty("user.dir");

	public static void main(String[] args) {
		NgitApplication gitClone = new NgitApplication();
		gitClone.processCommand(args);
	}

	private void processCommand(String[] input) {
		System.out.println(Arrays.toString(input));

		if (input.length < 1) {return; }

		String command = input[0];
		String argument = (input.length > 1) ? input[1] : null;

		switch (command) {
			case "init" ->  InitCommand.execute(GLOBAL_REPOSITORY_NAME);
			case "add" -> AddCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "checkout" -> CheckoutCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "commit" -> CommitCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "log" -> LogCommand.execute(GLOBAL_REPOSITORY_NAME);
			default -> System.out.println("Unknown ngit command");
		}
	}

	protected static FileTime getLastModifiedTime(Path path) {
		try {
			BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
			return attrs.lastModifiedTime();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected static void makeFolder(String folderName, String repositoryPath) {
		Path dirPath = Paths.get(repositoryPath + "/" + folderName);

		if (directoryExists(dirPath)) {
			return;
		}

		try {
			Files.createDirectory(dirPath);
		} catch (IOException e) {
			System.err.println("Failed to create directory: " + e.getMessage());
		}
	}

	protected static void makeFile(String fileName, String filePath) {
		Path parentDirPath = Path.of(filePath);
		Path absoluteFilePath = parentDirPath.resolve(fileName);

		if (Files.exists(absoluteFilePath)) {
			return;
		}

		try {
			Files.createDirectories(parentDirPath);
			Files.createFile(absoluteFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	protected static boolean directoryExists(Path directory) {
		return Files.exists(directory) && Files.isDirectory(directory);
	}
}
