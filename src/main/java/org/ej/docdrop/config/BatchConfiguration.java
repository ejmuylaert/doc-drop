package org.ej.docdrop.config;

import org.ej.docdrop.sync.DeviceSyncJobBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // When creating custom configuration, the  (global application) JPA transaction manager should be configured
    // explicitly, else the job and repositories don't work together nicely
    @Bean
    public BatchConfigurer batchConfigurer(JobRepository jobRepository, JpaTransactionManager transactionManager) {
        return new DefaultBatchConfigurer() {
            @Override
            public JobLauncher getJobLauncher() {
                SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
                jobLauncher.setJobRepository(jobRepository);
                jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
                try {
                    jobLauncher.afterPropertiesSet();
                } catch (Exception e) {
                    throw new RuntimeException("Error configuring JobLauncher", e);
                }
                return jobLauncher;
            }

            @Override
            public PlatformTransactionManager getTransactionManager() {
                return transactionManager;
            }
        };
    }

    @Bean
    public Job syncWithRemarkableJob(DeviceSyncJobBuilder jobBuilder) {
        return jobBuilder.build();
    }
}
