package org.ej.docdrop.domain;

import javax.persistence.Entity;
import java.util.UUID;

@Entity
public class DeleteCommand extends RemarkableCommand {

    protected DeleteCommand() {
    }

    public DeleteCommand(UUID fileId, long commandNumber) {
        super(fileId, commandNumber);
    }
}