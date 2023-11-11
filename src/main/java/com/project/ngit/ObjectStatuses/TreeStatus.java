package com.project.ngit.ObjectStatuses;

import java.io.Serializable;

/**
 * The TreeStatus record represents the status of a 'tree' object in the NGit version control system.
 * A 'tree' object can correspond to a directory or a file in the repository, storing a snapshot of its state.
 * This record is used to keep track of the name, hash, and type of the object.
 */
public record TreeStatus(
        /**
         * The name of the object, which can be a file name or directory name.
         */
        String name,

        /**
         * The SHA-2 hash of the object's contents. For a directory, this is typically the combined hash of its contents.
         */
        String hash,

        /**
         * The type of the object, which can be either "blob" for files or "tree" for directories.
         */
        String objectType
) implements Serializable {
    // The record implicitly declares final fields, a constructor, and accessor methods for all the fields.
}
