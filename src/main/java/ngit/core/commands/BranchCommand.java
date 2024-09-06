package ngit.core.commands;

import ngit.core.utils.Common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * The BranchCommand class is responsible for handling branch creation within an NGit repository.
 */
public class BranchCommand {
    private static final Logger LOGGER = Logger.getLogger(BranchCommand.class.getName());
    private static final String NGIT_DIR = ".ngit";
    private static final String HEADS_DIR = "heads";
    private final Path repositoryPath;

    /**
     * Constructs a BranchCommand with the specified repository path.
     *
     * @param repositoryPath the path to the repository where the branch will be created
     */
    public BranchCommand(String repositoryPath) {
        this.repositoryPath = Paths.get(repositoryPath);
    }

    /**
     * Executes the branch creation command using the provided branch name.
     *
     * @param nameOfBranch the name of the new branch to create
     */
    public void execute(String nameOfBranch) {
        if (nameOfBranch == null || nameOfBranch.trim().isEmpty()) {
            System.out.println("Invalid branch name provided.");
            return;
        }

        Path ngitPath = repositoryPath.resolve(NGIT_DIR);
        Path headsDir = ngitPath.resolve(HEADS_DIR);

        if (!Common.directoryExists(headsDir)) {
            System.out.println("The heads directory does not exist.");
            return;
        }

        try {
            Common.makeFile(headsDir.toString(), nameOfBranch, "");
        } catch (Exception e) {
            System.out.println("An error occurred while creating a branch.");
        }
    }
}
