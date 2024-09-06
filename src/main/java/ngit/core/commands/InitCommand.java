package ngit.core.commands;

import ngit.core.utils.Common;

/**
 * The InitCommand class is responsible for initializing a new NGit repository.
 * This process involves creating the necessary directory structure for the repository to function.
 */
public class InitCommand {
    private final String repositoryPath;

    /**
     * Constructs an InitCommand with the specified repository path.
     *
     * @param repositoryPath the path where the new NGit repository will be initialized
     */
    public InitCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    /**
     * Executes the initialization of the NGit repository.
     * It creates the .ngit directory along with subdirectories for objects, index, and heads.
     */
    public void execute() {
        // Create the main .ngit directory which will contain all the repository data
        Common.makeFolder(".ngit", repositoryPath);
        // Create the objects directory to store all the git objects like commits, trees, and blobs
        Common.makeFolder(".ngit/objects", repositoryPath);
        // Create the index directory to hold information about the current working directory state
        Common.makeFolder(".ngit/index", repositoryPath);
        // Create the heads directory to keep track of branch pointers
        Common.makeFolder(".ngit/heads", repositoryPath);
    }
}
