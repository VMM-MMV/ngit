package com.project.ngit.ObjectStatuses;

import java.io.Serializable;

/**
 * The BlobStatus record represents the status of a 'blob' object in the NGit version control system.
 * A 'blob' typically corresponds to a file in the repository.
 * This record stores metadata about a file, including its name, hash, and last modified date.
 */
public record BlobStatus(
        /**
         * The name of the file.
         */
        String name,

        /**
         * The SHA-2 hash of the file's contents, used for identifying the contents in the NGit system.
         */
        String fileHash,

        /**
         * The last modified date of the file, stored as a string.
         */
        String lastModifiedDate
) implements Serializable {
    // The record implicitly declares final fields, a constructor, and accessor methods for all the fields.
}
