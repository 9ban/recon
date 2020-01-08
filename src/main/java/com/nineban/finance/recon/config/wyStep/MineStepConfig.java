package com.nineban.finance.recon.config.wyStep;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.dto.ReconDTO;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicy;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicyRepository;
import com.nineban.finance.recon.repository.wb.FinancePremiumSummaryRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MineStepConfig {

    private final FinancePremiumPolicyRepository financePremiumPolicyRepository;

    public final StepBuilderFactory stepBuilderFactory;


    public MineStepConfig(FinancePremiumPolicyRepository financePremiumPolicyRepository, StepBuilderFactory stepBuilderFactory) {
        this.financePremiumPolicyRepository = financePremiumPolicyRepository;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public RepositoryItemReader<FinancePremiumPolicy> mineStepReader(@Value("#{jobParameters['cooperate_org_id']}") Long cooperateOrgId,
                                                                     @Value("#{jobParameters['start_time']}") Long startTime,
                                                                     @Value("#{jobParameters['end_time']}") Long endTime){
        //读取时间范围那，第三方剩下未对账的数据，这些是我方有第三方无的数据
        LocalDateTime localStartTime = getTime(startTime);
        LocalDateTime localEndTime = getTime(endTime);
        Map<String, Sort.Direction> map  = new HashMap<>(1);
        map.put("policySn", Sort.Direction.ASC);
        return new RepositoryItemReaderBuilder<FinancePremiumPolicy>()
                .name("mineStepReader")
                .repository(financePremiumPolicyRepository)
                .methodName("findByCooperateOrgIdAndTransTimeBetweenAndStatus")
                .sorts(map)
                .arguments(Arrays.asList(cooperateOrgId,localStartTime,localEndTime,FinancePremiumPolicy.STATUS_WAITING))
                .build();
    }

    @Bean
    public MineStepProcessor mineStepProcessor(){
        return new MineStepProcessor();
    }

    @Bean
    public FlatFileItemWriter<ReconDTO> mineStepDiffLogWriter(){
        FlatFileItemWriter<ReconDTO> wyDiffLogWriter = new FlatFileItemWriterBuilder<ReconDTO>()
                .name("mineStepDiffLogWriter")
                .resource(new FileSystemResource(ContextContant.DIFF_LOG_PATH))
                .headerCallback(mineStepDiffHeaderCallback())
                .append(true)
                .delimited()
                .delimiter(",")
                .names(new String[]{"policySn","amount","diffPolicySn","diffAmount","diffMsg"})
                .build();
        return wyDiffLogWriter;
    }

    @Bean
    public FlatFileHeaderCallback mineStepDiffHeaderCallback(){
        return writer -> writer.write("保司保单号,保司金额,微易保单号,微易金额,差异原因");
    }

    @Bean
    public Step mineStep(RepositoryItemReader<FinancePremiumPolicy> mineStepReader, MineStepProcessor mineStepProcessor, FlatFileItemWriter<ReconDTO> mineStepDiffLogWriter){
        Step step = stepBuilderFactory.get("wyStep")
                .<FinancePremiumPolicy, ReconDTO> chunk(10)
                .reader(mineStepReader)
                .processor(mineStepProcessor)
                .writer(mineStepDiffLogWriter)
                .stream(mineStepDiffLogWriter)
                .build();
        return step;
    }

    private LocalDateTime getTime(Long time){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }
}
