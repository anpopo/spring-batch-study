package io.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@RequiredArgsConstructor
@Configuration
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(step1())
                .next(step2())
                .next(step3())
                .next(step4())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {

                    ExecutionContext jobExecutionContext = stepContribution.getStepExecution().getJobExecution().getExecutionContext();
                    ExecutionContext stepExecutionContext = stepContribution.getStepExecution().getExecutionContext();

                    String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobInstance().getJobName();
                    String stepName = chunkContext.getStepContext().getStepExecution().getStepName();

                    if (jobExecutionContext.get("jobName") == null) {
                        jobExecutionContext.put("jobName", jobName);
                    }

                    if (stepExecutionContext.get("stepName") == null) {
                        stepExecutionContext.put("stepName", stepName);
                    }


                    System.out.println("step 1 was executed - job name : " + jobExecutionContext.get("jobName") + " / step name : " + stepExecutionContext.get("stepName"));
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {

                    ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
                    ExecutionContext stepExecutionContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();

                    System.out.println("step 2 was executed - job name : " + jobExecutionContext.get("jobName") + " / step name : " + stepExecutionContext.get("stepName"));

                    String stepName = chunkContext.getStepContext().getStepExecution().getStepName();

                    if(!stepExecutionContext.containsKey(stepName)) {
                        stepExecutionContext.put("stepName", stepName);
                        System.out.println("step 2 was executed - step name : " + stepExecutionContext.get("stepName"));
                    }

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet((stepContribution, chunkContext) -> {
                    Object name = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("name");

                    if(Objects.isNull(name)) {
                        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("name", "user1");
                    }

                    System.out.println("step 3 was executed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step4() {
        return stepBuilderFactory.get("step4")
                .tasklet((stepContribution, chunkContext) -> {

                    Object name = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("name");

                    System.out.println("step 4 was executed - name : " + name);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
