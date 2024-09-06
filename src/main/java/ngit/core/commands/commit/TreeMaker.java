package ngit.core.commands.commit;

import ngit.core.hash.SHA;
import ngit.core.statuses.BlobStatus;
import ngit.core.statuses.TreeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The TreeMaker class is responsible for creating tree objects that represent the state of a directory.
 * It walks through the directory structure of a repository and generates a SHA-2 hash for the contents
 * of each directory, which is then used to create a tree object.
 */
class TreeMaker {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreeMaker.class);
    private final Path objectsPath;
    private String headTree;
    private final Map<String, BlobStatus> existingData;

    /**
     * Constructs a new TreeMaker with the specified objects path and existing data map(file, where info about added files are stored).
     *
     * @param objectsPath  the path to the objects directory
     * @param existingData a map of existing blob statuses keyed by file path
     */
    public TreeMaker(Path objectsPath, Map<String, BlobStatus> existingData) {
        this.objectsPath = objectsPath;
        this.existingData = existingData;
    }

    /**
     * Gets the SHA-2 hash of the head tree.
     *
     * @return the SHA-2 hash of the head tree
     */
    public String getHeadTree() {
        return headTree;
    }

    /**
     * Generates trees from the directory structure starting from the given path.
     *
     * @param ngitPath the starting path to generate trees from
     */
    public void makeTrees(Path ngitPath) {
        List<Path> directories = listOfDirectories(ngitPath.getParent());
        Collections.reverse(directories);

        for (Path directory : directories) {
            makeOneTree(directory);
        }

        System.out.println(headTree);
    }

    /**
     * Lists all directories under the given root directory path.
     *
     * @param rootDirectoryPath the root directory path
     * @return a list of directory paths
     */
    private List<Path> listOfDirectories(Path rootDirectoryPath) {
        if (!Files.isDirectory(rootDirectoryPath)) {
            throw new IllegalArgumentException("The provided path is not a directory");
        }

        try {
            return Files.walk(rootDirectoryPath)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error traversing directories", e);
        }
    }

    /**
     * Creates a tree object from the specified directory path.
     *
     * @param directoryPath the directory path to create a tree for
     */
    private void makeOneTree(Path directoryPath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath)) {
            List<TreeStatus> treeInfo = collectTreeInfo(stream);

            if (!treeInfo.isEmpty()) {
                String shaOfDirectoryContents = computeDirectorySHA(treeInfo);
                headTree = shaOfDirectoryContents; // It's value will change constantly and the last value here will be the head of all the trees

                createTreeGitObject(shaOfDirectoryContents, treeInfo);
                existingData.put(String.valueOf(directoryPath), new BlobStatus(directoryPath.getFileName().toString(), shaOfDirectoryContents, "tree"));
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while listing files in directory: {}", directoryPath, e);
        }
    }

    /**
     * Collects information about the files and subdirectories in the given directory to create a tree status list.
     *
     * @param directoryStream the directory stream to collect tree information from
     * @return a list of tree statuses
     */
    private List<TreeStatus> collectTreeInfo(DirectoryStream<Path> directoryStream) {
        List<TreeStatus> treeInfo = new ArrayList<>();
        for (Path path : directoryStream) {
            if (existingData.containsKey(String.valueOf(path))) {
                BlobStatus fileStatus = existingData.get(String.valueOf(path));
                String objectType = Files.isDirectory(path) ? "tree" : "blob";
                treeInfo.add(new TreeStatus(fileStatus.name(), fileStatus.fileHash(), objectType));
            }
        }
        return treeInfo;
    }

    /**
     * Computes the SHA-2 hash for the contents of a directory based on the tree information.
     *
     * @param treeInfo the tree information to compute the hash for
     * @return the SHA-2 hash as a string
     */
    private String computeDirectorySHA(List<TreeStatus> treeInfo) {
        StringBuilder directoryContentsHash = new StringBuilder();
        for (TreeStatus status : treeInfo) {
            directoryContentsHash.append(status.hash());
        }
        return SHA.computeSHA(directoryContentsHash.toString());
    }

    /**
     * Creates a tree git object from the tree information
     *
     * @param shaOfDirectoryContents the SHA-2 hash of the directory contents
     * @param treeInfo               the tree information to write
     */
    protected void createTreeGitObject(String shaOfDirectoryContents, List<TreeStatus> treeInfo) {
        Path folderPath = objectsPath.resolve(shaOfDirectoryContents.substring(0, 2));
        Path treeFilePath = folderPath.resolve(shaOfDirectoryContents.substring(2));

        try {
            Files.createDirectories(folderPath);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(treeFilePath))) {
                oos.writeObject(treeInfo);
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while writing the tree to the file: {}", treeFilePath, e);
        }
    }
}
