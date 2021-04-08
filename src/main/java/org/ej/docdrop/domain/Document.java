package org.ej.docdrop.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private final String filepath;
    private final String originalName;
    private final String name;

    public Document() {
        this.filepath = null;
        this.originalName = null;
        this.name = null;
    }

    public Document(String filepath, String originalName, String name) {
        this.filepath = filepath;
        this.originalName = originalName;
        this.name = name;
    }

    public String getDisplayName() {
        return name == null ? originalName : name;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(filepath, document.filepath) && Objects.equals(originalName,
                document.originalName) && Objects.equals(name, document.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filepath, originalName, name);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", filepath='" + filepath + '\'' +
                ", originalName='" + originalName + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}