package org.ej.docdrop.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/sync")
public class SyncController {

    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;
    private final Job syncJob;

    public SyncController(JobExplorer jobExplorer, JobLauncher jobLauncher, Job syncJob) {
        this.jobExplorer = jobExplorer;
        this.jobLauncher = jobLauncher;
        this.syncJob = syncJob;
    }

    @GetMapping
    String index(Model model) {
        Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions("Sync with Remarkable");
        Optional<JobExecution> current_job = runningJobs.stream().findFirst();
        List<JobInstance> jobs = jobExplorer.getJobInstances("Sync with Remarkable", 0, 25);

        List<JobExecution> executions = jobs.stream()
                .map(jobExplorer::getJobExecutions)
                .flatMap(Collection::stream)
                .toList();

        model.addAttribute("currentJob", current_job);
        model.addAttribute("pastJobs", executions);

        return "sync/index";
    }

    @PostMapping
    String startSync() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {

        JobParameters parameters = new JobParametersBuilder(jobExplorer)
                .getNextJobParameters(syncJob)
                .toJobParameters();

        jobLauncher.run(syncJob, parameters);

        return "redirect:/sync";
    }
}
