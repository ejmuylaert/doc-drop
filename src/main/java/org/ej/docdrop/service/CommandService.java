package org.ej.docdrop.service;

import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

@Service
public class CommandService {

    private final RemarkableCommandRepository repository;
    private final Clock clock;

    public CommandService(RemarkableCommandRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Creates a command with new command number (increasing)
     * <p>
     * (must always be part of an existing transaction?
     *
     * @return the created command
     */
    RemarkableCommand createCommand() {
        return null;
    }

    /**
     * Fetches the next pending command from the database and marks it as being executed.
     *
     * @return the RemarkableCommand execute, or none if the last command is executed
     */
    Optional<RemarkableCommand> nextPendingCommand() {
        Iterable<RemarkableCommand> inProgressCommands = repository.findByExecutionStartedAtIsNotNullAndExecutedAtIsNull();
        if (inProgressCommands.iterator().hasNext()) {
            throw new RuntimeException("Already command begin executed");
        }

        Optional<RemarkableCommand> command = repository.findFirstByExecutionStartedAtIsNullAndExecutedAtIsNullOrderByCommandNumberAsc();
        command.ifPresent(c -> c.setExecutionStartedAt(clock.instant()));

        return command;
    }

    void markAsCompleted(RemarkableCommand command) {
        command.setExecutedAt(clock.instant());
        repository.save(command);
    }
}
