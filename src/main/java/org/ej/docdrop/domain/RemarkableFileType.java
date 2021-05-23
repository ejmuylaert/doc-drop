package org.ej.docdrop.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RemarkableFileType {

    @JsonProperty("pdf")
    PDF;

    @JsonCreator
    public static RemarkableFileType fromValue(String value) {
        return switch (value) {
            case "pdf" -> PDF;
            default -> throw new RuntimeException("Cannot create type from: " + value);
        };
    }
}
