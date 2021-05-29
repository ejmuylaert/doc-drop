package org.ej.docdrop.sync;

import org.ej.docdrop.domain.CreateFolderCommand;
import org.ej.docdrop.domain.SyncCommand;
import org.ej.docdrop.domain.SyncEvent;
import org.ej.docdrop.domain.UploadFileCommand;
import org.ej.docdrop.repository.SyncCommandRepository;
import org.ej.docdrop.service.RemarkableConnectionException;
import org.ej.docdrop.service.SyncCommandHandler;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeviceSyncJobBuilder {

    public static final String NAME = "Sync with Remarkable";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final SyncCommandRepository repository;
    private final SyncCommandHandler commandHandler;

    public DeviceSyncJobBuilder(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                SyncCommandRepository repository, SyncCommandHandler commandHandler) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.repository = repository;
        this.commandHandler = commandHandler;
    }

    public Job build() {
        return jobBuilderFactory.get(NAME)
                .preventRestart()
                .incrementer(new RunIdIncrementer())
                .start(applySyncCommands())
                .build();
    }

    private RepositoryItemReader<SyncCommand> commandReader() {
        return new RepositoryItemReaderBuilder<SyncCommand>()
                .repository(repository)
                .methodName("findAllBySyncedAtIsNull")
                .saveState(false)
                .maxItemCount(10)
                .pageSize(10)
                .sorts(Map.of("commandNumber", Sort.Direction.ASC))
                .build();
    }

    private RepositoryItemWriter<SyncCommand> commandWriter() {
        return new RepositoryItemWriterBuilder<SyncCommand>()
                .repository(repository)
                .methodName("updateResult")
                .build();
    }

    private Step applySyncCommands() {
        return stepBuilderFactory.get("1 - apply sync commands")
                .<SyncCommand, SyncCommand>chunk(1)
                .reader(commandReader())
                .processor(new CommandProcessor(commandHandler))
                .writer(commandWriter())
                .build();

    }

    private class CommandProcessor implements ItemProcessor<SyncCommand, SyncCommand> {

        private final SyncCommandHandler commandHandler;

        public CommandProcessor(SyncCommandHandler commandHandler) {
            this.commandHandler = commandHandler;
        }

        @Override
        public SyncCommand process(SyncCommand item) throws Exception {
            if (item instanceof CreateFolderCommand createFolderCommand) {
                try {
                    SyncEvent event = commandHandler.apply(createFolderCommand);
                    item.setResult(event);
                    return item;
                } catch (RemarkableConnectionException e) {
                    throw new RuntimeException("Connection problem", e);
                }
            } else if (item instanceof UploadFileCommand uploadFileCommand) {
                try {
                    SyncEvent event = commandHandler.apply(uploadFileCommand);
                    item.setResult(event);
                    return item;
                } catch (RemarkableConnectionException e) {
                    throw new RuntimeException("Connection problem", e);
                }
            } else {
                throw new RuntimeException("Unexpected command");
            }
        }
    }
}
