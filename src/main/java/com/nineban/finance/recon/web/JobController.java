package com.nineban.finance.recon.web;

import com.nineban.finance.recon.ContextContant;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RequestMapping(path =  "job")
@RestController
public class JobController {

    private final JobLauncher jobLauncher;

    private final Job reconJob;

    public JobController(JobLauncher jobLauncher, Job reconJob) {
        this.jobLauncher = jobLauncher;
        this.reconJob = reconJob;
    }

    @PostMapping(path = "launchJob")
    @Async
    public String launchJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        LocalDateTime localDateTime = LocalDateTime.of(2019,12,1,0,0);
        LocalDateTime endDateTime = LocalDateTime.of(2020,2,1,0,0);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong(ContextContant.COOPERATE_ORG_ID,10L)
                .addLong(ContextContant.BATCH_ID,20L)
                .addLong(ContextContant.START_TIME,localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() )
                .addLong(ContextContant.END_TIME,endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                //为了同一个任务能重复执行，设置时间戳作为参数
                .addLong("timestamp",System.currentTimeMillis())
//                .addString("fileName","sample-data.csv")
                .toJobParameters();
        jobLauncher.run(reconJob,jobParameters);
        return "job is start!";
    }
}
