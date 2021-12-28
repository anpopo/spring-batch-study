package io.springbatch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class BatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job batchJob1 () {
        return this.jobBuilderFactory.get("job1")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .next(step2())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("1");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("2");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Job batchJob2 () {
        return this.jobBuilderFactory.get("job2")
                .start(flow())
                .next(step5())
                .end().build();
    }

    @Bean
    public Flow flow () {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        return flowBuilder.start(step3())
                .next(step4())
                .end();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("3");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step step4() {
        return stepBuilderFactory.get("step4")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("4");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }

    @Bean
    public Step step5() {
        return stepBuilderFactory.get("step5")
                .tasklet(((stepContribution, chunkContext) -> {
                    System.out.println("5");
                    return RepeatStatus.FINISHED;
                }))
                .build();
    }
}
