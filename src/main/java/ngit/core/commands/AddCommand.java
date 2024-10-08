package ngit.core.commands;

import ngit.core.statuses.BlobStatus;
import ngit.core.hash.SHA;
import ngit.core.utils.Common;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.Deflater;

/**
 * The AddCommand class is responsible for adding files to the staging area in an NGit repository.
 * It processes files by computing their SHA-2 hash and storing their status.
 */
public class AddCommand {
    private final Path ngitPath;
    private final String repositoryPath;
    private Map<String, BlobStatus> existingData;

    /**
     * Constructs an AddCommand for a given repository path.
     *
     * @param repositoryPath the path to the repository where the command will be executed
     */
    public AddCommand(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        this.ngitPath = Path.of(repositoryPath, ".ngit");
        this.existingData = new HashMap<>();
    }

    /**
     * Executes the add command using the provided argument to determine the scope of the operation, then save the contents to changes.ser in the index folder.
     *
     * @param argument the argument specifying which files to add; "." indicates all files
     */
    public void execute(String argument) {
        existingData = Common.readExistingData(ngitPath.resolve("index/changes.ser"));

        assert argument != null;
        if (argument.equals(".")) {
            processAllFilesInRepository();
        } else {
            processSingleFile(argument);
        }

        Common.saveDataToFile(ngitPath.resolve("index/changes.ser"), existingData);
    }

    /**
     * Processes all files in the repository for addition to the staging area.
     */
    private void processAllFilesInRepository() {
        try (Stream<Path> stream = Files.walk(Path.of(repositoryPath))) {
            stream.forEach(this::processPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Processes a single file for addition to the staging area.
     *
     * @param argument the relative path to the file from the repository root
     */
    private void processSingleFile(String argument) {
        Path filePath = Path.of(repositoryPath).resolve(argument);
        processPath(filePath);
    }

    /**
     * Processes the given path and adds it to the staging area if it is a file.
     *
     * @param path the path to the file to be processed
     */
    private void processPath(Path path) {
        if (Files.isDirectory(path) || String.valueOf(path).contains(".ngit") || String.valueOf(path).contains(".git")) {
            return;
        }

        FileTime lastModifiedTime = Common.getLastModifiedTime(path);

        try {
            String gitObjectHash = addBlob(String.valueOf(ngitPath), String.valueOf(path));

            BlobStatus blobStatus = new BlobStatus(path.getFileName().toString(), gitObjectHash, lastModifiedTime.toString());
            existingData.put(path.toString(), blobStatus);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a blob to the NGit object store for the given file path.
     *
     * @param ngitPath the path to the NGit directory
     * @param path     the path to the file to add as a blob
     * @return the SHA-2 hash of the added file
     * @throws IOException if an I/O error occurs
     */
    public String addBlob(String ngitPath, String path) throws IOException {
        String shaOfFile = SHA.fileToSHA(path);
        String gitObjectDirectory = shaOfFile.substring(0, 2);
        String gitObjectName = shaOfFile.substring(2);

        String objectPath = String.valueOf(Path.of(ngitPath, "objects", gitObjectDirectory));

        byte[] fileContents = Files.readAllBytes(Paths.get(path));
        byte[] compressedContents = compress(fileContents);

        Common.makeFile(objectPath, gitObjectName, "");

        String fullPath = String.valueOf(Path.of(objectPath, gitObjectName));
        Files.write(Paths.get(fullPath), compressedContents, StandardOpenOption.CREATE);

        return shaOfFile;
    }

    /**
     * Compresses the given byte array using the Deflater algorithm. This encrypts the file contents for storage improvements.
     *
     * @param data the data to compress
     * @return the compressed data as a byte array
     * @throws IOException if an I/O error occurs during compression
     */
    private byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }
    }
}
