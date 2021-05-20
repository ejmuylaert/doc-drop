package org.ej.docdrop.job;


import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.SyncCommand;
import org.ej.docdrop.domain.SyncEvent;
import org.ej.docdrop.domain.SyncResult;
import org.ej.docdrop.repository.SyncCommandRepository;
import org.ej.docdrop.service.ConnectionException;
import org.ej.docdrop.service.SyncCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ej.docdrop.domain.SyncResult.EXECUTION_FAILED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@SpringBatchTest
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SyncJobTest extends AbstractDatabaseTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private SyncCommandRepository commandRepository;

    @MockBean
    private SyncCommandHandler commandHandler;

    @BeforeEach
    void cleanDatabase() {
        commandRepository.deleteAll();
    }

    @Test
    @DisplayName("Save successful command execution to database")
    void saveSuccess() throws Throwable {
        // Given
        CreateFolderCommand command1 = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);
        CreateFolderCommand command2 = new CreateFolderCommand(UUID.randomUUID(), 2L, "name", null);
        commandRepository.save(command1);
        commandRepository.save(command2);

        when(commandHandler.apply(any(CreateFolderCommand.class)))
                .thenAnswer(invocation -> SyncEvent.create(invocation.getArgument(0), SyncResult.SUCCESS, ""));

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        StepExecution stepExecution = jobExecution.getStepExecutions().stream().findFirst().get();

        BatchStatus status = jobExecution.getStatus();
        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(stepExecution.getReadCount()).isEqualTo(2);
        assertThat(stepExecution.getWriteCount()).isEqualTo(2);
        assertThat(stepExecution.getFilterCount()).isEqualTo(0);

        SyncCommand updatedCmd1 = commandRepository.findById(new SyncCommand.CommandId(command1.getFileId(), command1.getCommandNumber())).get();
        SyncCommand updatedCmd2 = commandRepository.findById(new SyncCommand.CommandId(command2.getFileId(), command2.getCommandNumber())).get();

        assertThat(updatedCmd1.getSyncResult()).isEqualTo(SyncResult.SUCCESS);
        assertThat(updatedCmd2.getSyncResult()).isEqualTo(SyncResult.SUCCESS);
    }

    @Test
    @DisplayName("Save error with message to database & continue")
    void saveErrorAndContinue() throws Exception {
        // Given
        CreateFolderCommand command1 = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);
        CreateFolderCommand command2 = new CreateFolderCommand(UUID.randomUUID(), 2L, "name", null);
        commandRepository.save(command1);
        commandRepository.save(command2);

        when(commandHandler.apply(any(CreateFolderCommand.class))) .thenAnswer(invocation -> {
            SyncCommand command = invocation.getArgument(0);
            if (command.getCommandNumber() == 1L) {
                return SyncEvent.create(command1, EXECUTION_FAILED, "message");
            } else {
                return SyncEvent.create(command, SyncResult.SUCCESS, "");
            }
        });

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // Then
        SyncCommand updatedCmd1 = commandRepository.findById(new SyncCommand.CommandId(command1.getFileId(), command1.getCommandNumber())).get();
        SyncCommand updatedCmd2 = commandRepository.findById(new SyncCommand.CommandId(command2.getFileId(), command2.getCommandNumber())).get();

        System.out.println(jobExecution.getExitStatus());

        assertThat(updatedCmd1.getSyncResult()).isEqualTo(EXECUTION_FAILED);
        assertThat(updatedCmd1.getSyncMessage()).isEqualTo("message");
        assertThat(updatedCmd2.getSyncResult()).isEqualTo(SyncResult.SUCCESS);
        assertThat(updatedCmd2.getSyncMessage()).isEqualTo("");
    }

    @Test
    @DisplayName("Only single execution of task")
    void onlySingleExecution() {

    }

    @Test
    @DisplayName("second run gets the new commands")
    void secondRunWithNewCommands() throws Exception {
        // Given
        when(commandHandler.apply(any(CreateFolderCommand.class)))
                .thenAnswer(invocation -> SyncEvent.create(invocation.getArgument(0), SyncResult.SUCCESS, ""));

        CreateFolderCommand command1 = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);
        CreateFolderCommand command2 = new CreateFolderCommand(UUID.randomUUID(), 2L, "name", null);
        commandRepository.save(command1);
        commandRepository.save(command2);

        JobExecution firstJobExecution = jobLauncherTestUtils.launchJob();

        CreateFolderCommand command3 = new CreateFolderCommand(UUID.randomUUID(), 3L, "name", null);
        commandRepository.save(command3);

        // When
        JobExecution secondExecution = jobLauncherTestUtils.launchJob();
        StepExecution stepExecution = secondExecution.getStepExecutions().stream().findFirst().get();

        // Then
        assertThat(secondExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(stepExecution.getReadCount()).isEqualTo(1);
        assertThat(stepExecution.getWriteCount()).isEqualTo(1);
        assertThat(stepExecution.getFilterCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Abort job when connection not available")
    void abortJobWhenConnectionNotAvailable() throws Exception {
        // Given
        CreateFolderCommand command1 = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);
        CreateFolderCommand command2 = new CreateFolderCommand(UUID.randomUUID(), 2L, "name", null);
        commandRepository.save(command1);
        commandRepository.save(command2);

        when(commandHandler.apply(any(CreateFolderCommand.class))).thenThrow(new ConnectionException("No ...", null));


        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        StepExecution stepExecution = jobExecution.getStepExecutions().stream().findFirst().get();

        BatchStatus status = jobExecution.getStatus();
        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(stepExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(stepExecution.getReadCount()).isEqualTo(2);
        assertThat(stepExecution.getWriteCount()).isEqualTo(2);
        assertThat(stepExecution.getFilterCount()).isEqualTo(0);

        SyncCommand updatedCmd1 = commandRepository.findById(new SyncCommand.CommandId(command1.getFileId(), command1.getCommandNumber())).get();
        SyncCommand updatedCmd2 = commandRepository.findById(new SyncCommand.CommandId(command2.getFileId(), command2.getCommandNumber())).get();

        assertThat(updatedCmd1.getSyncResult()).isEqualTo(SyncResult.SUCCESS);
        assertThat(updatedCmd2.getSyncResult()).isEqualTo(SyncResult.SUCCESS);
    }
}