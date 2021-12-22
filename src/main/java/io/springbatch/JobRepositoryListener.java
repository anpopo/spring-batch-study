package io.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobRepositoryListener implements JobExecutionListener {

    private final JobRepository jobRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        JobParameters jobParameters = jobExecution.getJobParameters();

        JobExecution lastJobExecution = jobRepository.getLastJobExecution(jobName, jobParameters);

        if (lastJobExecution != null) {
            for (StepExecution st : lastJobExecution.getStepExecutions()) {
                BatchStatus status = st.getStatus();
                ExitStatus exitStatus = st.getExitStatus();

                System.out.println(exitStatus + " / " + status + " ? " +  st.getStepName());
            }
        }

    }
}
