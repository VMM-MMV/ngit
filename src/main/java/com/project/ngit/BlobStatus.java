package com.project.ngit;

import java.io.Serializable;

public record BlobStatus(String name, String fileHash, String lastModifiedDate) implements Serializable {
}
