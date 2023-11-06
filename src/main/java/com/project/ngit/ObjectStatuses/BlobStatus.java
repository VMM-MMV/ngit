package com.project.ngit.ObjectStatuses;

import java.io.Serializable;

public record BlobStatus(String name, String fileHash, String lastModifiedDate) implements Serializable {
}
