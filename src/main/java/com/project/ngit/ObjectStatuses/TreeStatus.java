package com.project.ngit.ObjectStatuses;

import java.io.Serializable;

public record TreeStatus(String name, String hash, String objectType) implements Serializable {
}
