package com.nineban.finance.recon.config;


import com.nineban.finance.recon.ContextContant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableBatchProcessing
@Configuration
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public Step thirdPartStep;

    @Autowired
    private Step mineStep;

    @Autowired
    private JobListener jobListener;

    Logger logger  = LoggerFactory.getLogger(BatchConfiguration.class);


    @Bean
    public Job reconJob(){
        return jobBuilderFactory.get("reconJob")
                .incrementer(new RunIdIncrementer())
                .start(thirdPartStep)
                .next(decider()).on(FlowExecutionStatus.COMPLETED.getName()).to(mineStep).end()
                .listener(jobListener)
                .build();
    }


    @Bean
    public JobExecutionDecider decider(){
        return (jobExecution, stepExecution) -> {
            boolean readError = (boolean)jobExecution.getExecutionContext().get(ContextContant.COMPANY_STEP_READ_ERROR);
            if(readError){
                logger.info("fail");
                return FlowExecutionStatus.FAILED;
            }else{
                logger.info("complete");
                return FlowExecutionStatus.COMPLETED;
            }
        };
    }

}
