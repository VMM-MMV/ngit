package com.project.ngit;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

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
		String argument = (input.length > 1)
					      ? String.join(" ", Arrays.copyOfRange(input, 1, input.length))
					      : null;

		String[] parts = new String[0];
		if (argument != null && argument.contains(" to ")) {
			parts = argument.split(" to ", 2);
		}

		switch (command) {
			case "init" ->  InitCommand.execute(GLOBAL_REPOSITORY_NAME);
			case "add" -> AddCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "checkout" -> CheckoutCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "commit" -> CommitCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "log" -> LogCommand.execute(GLOBAL_REPOSITORY_NAME);
			case "branch" -> BranchCommand.execute(GLOBAL_REPOSITORY_NAME, argument);
			case "rebase" -> RebaseCommand.execute(GLOBAL_REPOSITORY_NAME, parts[0], parts[1]);
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

	protected static void makeFile(String filePath, String fileName , String content) {
		Path parentDirPath = Path.of(filePath);
		Path absoluteFilePath = parentDirPath.resolve(fileName);

		try {
			// Create the directories if they do not exist
			Files.createDirectories(parentDirPath);

			// Write the content to the file, overwriting existing content
			Files.writeString(absoluteFilePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static boolean directoryExists(Path directory) {
		return Files.exists(directory) && Files.isDirectory(directory);
	}

	static Map<String, BlobStatus> readExistingData(Path filePath) {
		if (!Files.exists(filePath)) {
			return new HashMap<>();
		}

		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
			return (Map<String, BlobStatus>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to read existing data", e);
		}
	}

	static void saveDataToFile(Path filePath, Map<String, BlobStatus> data) {
		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
			oos.writeObject(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
