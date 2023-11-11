package com.project.ngit.ObjectStatuses;

import java.io.Serializable;

/**
 * The CommitStatus record represents the status of a commit in the NGit version control system.
 * It encapsulates the details of a commit, including references to the current and previous commits,
 * the creator of the commit, the commit's content, and the commit message.
 */
public record CommitStatus(
        /**
         * The SHA-2 hash of the current commit.
         */
        String currentCommit,

        /**
         * The SHA-2 hash of the previous commit. This is used to form the commit chain in the version history.
         */
        String previousCommit,

        /**
         * The name or identifier of the creator of the commit.
         */
        String creator,

        /**
         * The content of the commit, which include references to the tree objects that represent the directory structure.
         */
        String content,

        /**
         * The message associated with the commit, providing a description of the changes or the purpose of the commit.
         */
        String message
) implements Serializable {
    // The record implicitly declares final fields, a constructor, and accessor methods for all the fields.
}
