package ngit.core;

import ngit.core.commands.*;
import ngit.core.commands.commit.CommitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * The NgitApplication class serves as the entry point for the NGit version control system.
 * It processes user commands and delegates to the appropriate command classes.
 */
public class NgitApplication {
	final static String GLOBAL_REPOSITORY_NAME = System.getProperty("user.dir");
	private static final Logger LOGGER = LoggerFactory.getLogger(NgitApplication.class);

	/**
	 * The main method that starts the application.
	 * @param args Command line arguments passed to the application.
	 */
	public static void main(String[] args) {
		NgitApplication gitClone = new NgitApplication();
		gitClone.processCommand(args);
	}

	/**
	 * Processes the given command input and executes the corresponding NGit command.
	 * @param input The command input array.
	 */
	private void processCommand(String[] input) {
		LOGGER.info("Processing command: {}", Arrays.toString(input));

		if (input.length < 1) {
			System.out.println("No command provided");
			return;
		}

		String command = input[0];
		String argument = (input.length > 1)
				? String.join(" ", Arrays.copyOfRange(input, 1, input.length))
				: null;

		String[] parts = new String[0];
		if (argument != null && argument.contains(" to ")) {
			parts = argument.split(" to ", 2);
		}

		switch (command) {
			case "init" -> new InitCommand(GLOBAL_REPOSITORY_NAME).execute();
			case "add" -> new AddCommand(GLOBAL_REPOSITORY_NAME).execute(argument);
			case "checkout" -> new CheckoutCommand(GLOBAL_REPOSITORY_NAME).execute(argument);
			case "commit" -> new CommitCommand(GLOBAL_REPOSITORY_NAME).execute(argument);
			case "log" -> new LogCommand(GLOBAL_REPOSITORY_NAME).execute();
			case "branch" -> new BranchCommand(GLOBAL_REPOSITORY_NAME).execute(argument);
			case "rebase" -> new RebaseCommand(GLOBAL_REPOSITORY_NAME).execute(parts[0], parts[1]);
			default -> System.out.println("Unknown ngit command: " + command);
		}
	}
}
