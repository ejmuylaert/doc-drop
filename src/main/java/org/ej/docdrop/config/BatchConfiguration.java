package org.ej.docdrop.config;

import org.ej.docdrop.domain.RemarkableCommand;
import org.ej.docdrop.repository.RemarkableCommandRepository;
import org.ej.docdrop.service.RemarkableClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.UUID;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // When creating custom configuration, the  (global application) JPA transaction manager should be configured
    // explicitly, else the job and repositories don't work together nicely
//    @Bean
//    public BatchConfigurer batchConfigurer(JobRepository jobRepository, JpaTransactionManager transactionManager) {
//        return new DefaultBatchConfigurer() {
//            @Override
//            public JobLauncher getJobLauncher() {
//                SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
//                jobLauncher.setJobRepository(jobRepository);
//                jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
//                try {
//                    jobLauncher.afterPropertiesSet();
//                } catch (Exception e) {
//                    throw new RuntimeException("Error configuring JobLauncher", e);
//                }
//                return jobLauncher;
//            }
//
//            @Override
//            public PlatformTransactionManager getTransactionManager() {
//                return transactionManager;
//            }
//        };
//    }

    @Bean
    public RepositoryItemReader<RemarkableCommand> reader(RemarkableCommandRepository repository) {

        return new RepositoryItemReaderBuilder<RemarkableCommand>()
                .repository(repository)
                .methodName("findAllByExecutionStartedAtIsNullAndExecutedAtIsNull")
                .saveState(false)
                .maxItemCount(10)
                .pageSize(10)
                .sorts(Map.of("commandNumber", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<RemarkableCommand, RemarkableCommand> processor(RemarkableClient client) {

        return item -> {
            System.out.println(client);
            try {
                System.out.println(client.folderExists(UUID.randomUUID()));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            System.out.println("Processing command: " + item);
            return item;
        };
    }

    @Bean
    public RepositoryItemWriter<RemarkableCommand> writer(RemarkableCommandRepository repository) {

        return new RepositoryItemWriterBuilder<RemarkableCommand>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Bean
    public Job syncWithRemarkableJob(Step applySyncCommands) {
        return jobBuilderFactory.get("Sync with Remarkable")
                .preventRestart()
                .incrementer(new RunIdIncrementer())
                .start(applySyncCommands)
                .build();
    }

    @Bean
    public Step applySyncCommands(ItemReader<RemarkableCommand> reader, ItemWriter<RemarkableCommand> writer,
                                  ItemProcessor<RemarkableCommand, RemarkableCommand> processor) {
        return stepBuilderFactory.get("1 - apply sync commands")
                .<RemarkableCommand, RemarkableCommand>chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
