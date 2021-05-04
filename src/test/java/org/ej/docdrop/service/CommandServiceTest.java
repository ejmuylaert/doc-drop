package org.ej.docdrop.service;


import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate"
})
class CommandServiceTest extends AbstractDatabaseTest {

    private final Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final CommandService service;
    private final RemarkableCommandRepository repository;

    public CommandServiceTest(@Autowired RemarkableCommandRepository repository) {
        this.repository = repository;
        this.service = new CommandService(repository, fixedClock);
    }

    @Nested
    @DisplayName("Get next pending command")
    class NextCommand {

        @Test
        @DisplayName("Updates starting time")
        void updateStartingTime() {
            // Given
            UUID fileId = UUID.randomUUID();
            CreateFolderCommand command = new CreateFolderCommand(fileId, 0, "dummy", null);
            repository.save(command);

            // When
            Optional<RemarkableCommand> pendingCommand = service.nextPendingCommand();
            Optional<RemarkableCommand> updatedCommand = repository.findById(new RemarkableCommand.CommandId(fileId, 0));

            // Then
            assertThat(pendingCommand).hasValueSatisfying(c -> {
                assertThat(c.getFileId()).isEqualTo(fileId);
                assertThat(c.getExecutionStartedAt()).isEqualTo(fixedClock.instant());
                assertThat(command.getExecutedAt()).isNull();
            });
            assertThat(updatedCommand).hasValueSatisfying(c -> {
                assertThat(c.getFileId()).isEqualTo(fileId);
                assertThat(c.getExecutionStartedAt()).isEqualTo(fixedClock.instant());
                assertThat(command.getExecutedAt()).isNull();
            });
        }

        @Test
        @DisplayName("Picks the first pending command")
        void pickFirstPending() {
            // Given
            CreateFolderCommand firstCommand = new CreateFolderCommand(UUID.randomUUID(), 0, "dummy", null);
            firstCommand.setExecutionStartedAt(Instant.now());
            firstCommand.setExecutedAt(Instant.now());
            repository.save(firstCommand);
            UUID fileId = UUID.randomUUID();
            CreateFolderCommand command = new CreateFolderCommand(fileId, 1, "dummy", null);
            repository.save(command);

            // When
            Optional<RemarkableCommand> pendingCommand = service.nextPendingCommand();

            // Then
            assertThat(pendingCommand).hasValueSatisfying(c -> {
                assertThat(c.getFileId()).isEqualTo(fileId);
                assertThat(c.getExecutionStartedAt()).isEqualTo(fixedClock.instant());
                assertThat(command.getExecutedAt()).isNull();
            });
        }

        @Test
        @DisplayName("Returns 'none' when there are no available commands anymore")
        void returnNoneWhenThereAreNoPendingCommands() {
            // Given
            CreateFolderCommand firstCommand = new CreateFolderCommand(UUID.randomUUID(), 0, "dummy", null);
            firstCommand.setExecutionStartedAt(Instant.now());
            firstCommand.setExecutedAt(Instant.now());
            repository.save(firstCommand);

            // When
            Optional<RemarkableCommand> pendingCommand = service.nextPendingCommand();

            // Then
            assertThat(pendingCommand).isEmpty();
        }

        @Test
        @DisplayName("Throw error when there is already command being executed")
        void throwWhenAlreadyExecuting() {
            // Given
            CreateFolderCommand command = new CreateFolderCommand(UUID.randomUUID(), 0, "dummy", null);
            command.setExecutionStartedAt(Instant.now());
            repository.save(command);

            // When, Then
            assertThatThrownBy(service::nextPendingCommand).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Mark command as completed")
    class MarkAsCompleted {

        @Test
        @DisplayName("records the current instant when command is completed")
        void markAsCompleted() {
            // Given
            UUID fileId = UUID.randomUUID();
            CreateFolderCommand command = new CreateFolderCommand(fileId, 0, "dummy", null);
            command.setExecutionStartedAt(Instant.now());
            repository.save(command);

            // When
            service.markAsCompleted(command);
            Optional<RemarkableCommand> retrievedCommand = repository.findById(new RemarkableCommand.CommandId(fileId, 0));

            // Then
            assertThat(retrievedCommand).hasValueSatisfying(c ->
                    assertThat(c.getExecutedAt()).isEqualTo(fixedClock.instant()));
        }
    }
}
