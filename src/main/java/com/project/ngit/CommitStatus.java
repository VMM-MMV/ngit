package com.project.ngit;

import java.io.Serializable;

public record CommitStatus(String currentCommit, String previousCommit, String creator, String message) implements Serializable {
}
