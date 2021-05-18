package org.ej.docdrop.job;


import org.ej.docdrop.AbstractDatabaseTest;
import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.repository.SyncCommandRepository;
import org.ej.docdrop.service.RemarkableClient;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@SpringBatchTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SyncJobTest extends AbstractDatabaseTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private SyncCommandRepository commandRepository;

    @MockBean
    private RemarkableClient remarkableClient;

    @Test
    void something() throws Throwable {
        CreateFolderCommand command1 = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);
        CreateFolderCommand command2 = new CreateFolderCommand(UUID.randomUUID(), 1L, "name", null);
        commandRepository.save(command1);
        commandRepository.save(command2);

        when(remarkableClient.folderExists(any())).thenReturn(true);

        jobLauncherTestUtils.launchJob();
    }
}