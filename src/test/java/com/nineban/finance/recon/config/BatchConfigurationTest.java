package com.nineban.finance.recon.config;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.ReconApplication;
import com.nineban.finance.recon.repository.wb.FinancePremiumSummary;
import com.nineban.finance.recon.repository.wb.FinancePremiumSummaryRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@SpringBatchTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes= ReconApplication.class)
public class BatchConfigurationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private FinancePremiumSummaryRepository financePremiumSummaryRepository;

    @Test
    public void reconJob() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.of(2019,12,1,0,0);
        LocalDateTime endDateTime = LocalDateTime.of(2020,2,1,0,0);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong(ContextContant.COOPERATE_ORG_ID,10l)
                .addLong(ContextContant.BATCH_ID,20l)
                .addLong(ContextContant.START_TIME,localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() )
                .addLong(ContextContant.END_TIME,endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
//                .addString("fileName","sample-data.csv")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        List<FinancePremiumSummary> financePremiumSummaryList=  financePremiumSummaryRepository.findAll();

        Assert.assertEquals(financePremiumSummaryList.size(), 1);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}