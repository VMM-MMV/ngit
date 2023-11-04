package com.project.ngit;

import java.io.Serializable;

public record TreeStatus(String name, String hash, String objectType) implements Serializable {
}
