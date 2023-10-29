package com.project.ngit;

import java.io.Serializable;

public record FileStatus(String filePath, String fileName, String lastModifiedDate) implements Serializable {
}
