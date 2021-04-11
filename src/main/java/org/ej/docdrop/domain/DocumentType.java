package org.ej.docdrop.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum DocumentType {

    @JsonProperty("DocumentType")
    DOCUMENT,
    @JsonProperty("CollectionType")
    FOLDER;

    @JsonCreator
    public static DocumentType fromValue(String value) {
        return switch (value) {
            case "DocumentType" -> DOCUMENT;
            case "CollectionType" -> FOLDER;
            default -> throw new RuntimeException("Cannot create type from: " + value);
        };
    }
}