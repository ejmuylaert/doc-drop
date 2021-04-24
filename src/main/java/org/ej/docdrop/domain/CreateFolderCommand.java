package org.ej.docdrop.domain;

import org.ej.docdrop.service.RemarkableClient;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
public class CreateFolderCommand extends RemarkableCommand {

    private final String name;

    protected CreateFolderCommand() {
        this.name = null;
    }

    public CreateFolderCommand(UUID fileId, long commandNumber, String name) {
        super(fileId, commandNumber);

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void execute(RemarkableClient client) {

    }
}