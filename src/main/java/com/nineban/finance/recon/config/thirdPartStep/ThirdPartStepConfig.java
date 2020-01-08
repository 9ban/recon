package com.nineban.finance.recon.config.thirdPartStep;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.dto.FlatFileDTO;
import com.nineban.finance.recon.dto.ReconDTO;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicy;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicyRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class ThirdPartStepConfig {

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("baoxianDataSource")
    private DataSource baoxianDataSource;

    @Bean
    @StepScope
    public Resource thirdPartResource(@Value("#{jobParameters['fileName']}") String fileName) throws MalformedURLException {
        if (StringUtils.isEmpty(fileName)) {
            return new ClassPathResource("sample-data.csv");
        } else if(fileName.startsWith("http")){
            return new FileUrlResource(fileName);
        } else {
            return new ClassPathResource(fileName);
        }
    }

    /**
     * 启动任务时，传入第三方对账文件，文件内容为订单号和金额
     * @param companyResource
     * @return
     */
    @Bean
    public FlatFileItemReader<FlatFileDTO> thirdPartStepReader(Resource companyResource) {
        return new FlatFileItemReaderBuilder<FlatFileDTO>()
                .name("thirdPartStepReader")
                .resource(companyResource)
                .delimited()
                .names(new String[]{"policySn", "amount"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<FlatFileDTO>() {{
                    setTargetType(FlatFileDTO.class);
                }})
                .linesToSkip(1)
                .build();
    }

    @Bean
    public ThirdPartStepProcessor thirdPartStepProcessor(){
        return new ThirdPartStepProcessor();
    }

    @Bean
    public ClassifierCompositeItemWriter<ReconDTO> classifierCompositeItemWriter(FlatFileItemWriter<ReconDTO> thirdPartErrorLogWriter,FlatFileItemWriter<ReconDTO> thirdPartDiffLogWriter,JdbcBatchItemWriter<ReconDTO> thirdPartStepSameDbWriter,CompositeItemWriter<ReconDTO> thirdPartStepDiffCompositeWriter,CompositeItemWriter<ReconDTO> thirdPartStepAllCompositeWriter){
        return new ClassifierCompositeItemWriterBuilder<ReconDTO>()
                .classifier((Classifier<ReconDTO, ItemWriter<? super ReconDTO>>) reconDTO -> {
                    //如果读取文件错误。将内容写入target/test-outputs/output.csv文件中
                    if(ReconDTO.COMPARE_RESULT_ERROR.equals(reconDTO.getCompareResult())){
                        return thirdPartErrorLogWriter;
                        //如果对比结果是第三方有我方无，将内容写入target/test-outputs/diff.csv文件中
                    }else if(ReconDTO.COMPARE_RESULT_THIRD_PART_PLUS.equals(reconDTO.getCompareResult())){
                        return thirdPartDiffLogWriter;
                        //如果对比结果一致，设置订单的状态为对账中，同时设置批次id
                    }else if(ReconDTO.COMPARE_RESULT_SAME.equals(reconDTO.getCompareResult())){
                        return thirdPartStepSameDbWriter;
                        //如果对比结果不一致，设置订单状态为对账中，不设置批次id，同时将内容写入target/test-outputs/diff.csv文件中
                    }else if(ReconDTO.COMPARE_RESULT_DIFF.equals(reconDTO.getCompareResult())){
                        return thirdPartStepDiffCompositeWriter;
                        //如果对账结果既有一致又有不一致，将订单列表设置为对账中，为相同列表设置批次id，不相同列表不设置，同时将不相同的内容写入target/test-outputs/diff.csv文件中
                    }else if(ReconDTO.COMPARE_RESULT_SAME_AND_DIFF.equals(reconDTO.getCompareResult())){
                        return thirdPartStepAllCompositeWriter;
                    }
                    return null;
                }).build();
    }

    @Bean
    public FlatFileItemWriter<ReconDTO> thirdPartErrorLogWriter(){
        FlatFileItemWriter<ReconDTO>  errorLogWriter = new FlatFileItemWriterBuilder<ReconDTO>()
                .name("thirdPartErrorLogWriter")
                .resource(new FileSystemResource(ContextContant.ERROR_LOG_PATH))
                .headerCallback(thirdPartStepHeaderCallback())
                .delimited()
                .delimiter(",")
                .names(new String[]{"policySn","amountString","error"})
                .build();
        return errorLogWriter;
    }

    @Bean
    public CompositeItemWriter<ReconDTO> thirdPartStepDiffCompositeWriter(FlatFileItemWriter<ReconDTO> thirdPartErrorLogWriter,JdbcBatchItemWriter<ReconDTO> thirdPartStepDiffDbWriter){
        return new CompositeItemWriterBuilder<ReconDTO>().delegates(Arrays.asList(thirdPartErrorLogWriter,thirdPartStepDiffDbWriter))
                .build();
    }

    @Bean
    public CompositeItemWriter<ReconDTO> thirdPartStepAllCompositeWriter(FlatFileItemWriter<ReconDTO> thirdPartErrorLogWriter, JdbcBatchItemWriter<ReconDTO> thirdPartStepDiffDbWriter, JdbcBatchItemWriter<ReconDTO> thirdPartStepSameDbWriter){
        return new CompositeItemWriterBuilder<ReconDTO>().delegates(Arrays.asList(thirdPartErrorLogWriter,thirdPartStepDiffDbWriter,thirdPartStepSameDbWriter))
                .build();
    }

    @Bean
    public FlatFileItemWriter<ReconDTO> thirdPartDiffLogWriter(){
        FlatFileItemWriter<ReconDTO> diffLogWriter = new FlatFileItemWriterBuilder<ReconDTO>()
                .name("thirdPartDiffLogWriter")
                .resource(new FileSystemResource(ContextContant.DIFF_LOG_PATH))
                .headerCallback(thirdPartStepDiffHeaderCallback())
                .delimited()
                .delimiter(",")
                .names(new String[]{"policySn","amount","diffPolicySn","diffAmount","diffMsg"})
                .build();
        return diffLogWriter;
    }

    @Bean
    public JdbcBatchItemWriter<ReconDTO> thirdPartStepSameDbWriter(@Qualifier("baoxianDataSource") DataSource baoxianDataSource){
        return new JdbcBatchItemWriterBuilder<ReconDTO>()
                .sql("update finance_premium_policy set batch_id=:batchId,status=:status where id in (:idList)")
                .dataSource(baoxianDataSource).itemSqlParameterSourceProvider(reconDTO -> new MapSqlParameterSource().addValue("batchId",reconDTO.getBatchId())
                        .addValue("status", FinancePremiumPolicy.STATUS_ING)
                        .addValue("idList",reconDTO.getSameList().stream().map(FinancePremiumPolicy::getId).collect(Collectors.toList()))).build();
    }

    @Bean
    public JdbcBatchItemWriter<ReconDTO> thirdPartStepDiffDbWriter(){
        return new JdbcBatchItemWriterBuilder<ReconDTO>()
                .sql("update finance_premium_policy set status=:status where id in (:idList)")
                .dataSource(baoxianDataSource).itemSqlParameterSourceProvider(reconDTO -> new MapSqlParameterSource()
                        .addValue("status",FinancePremiumPolicy.STATUS_ING)
                        .addValue("idList",reconDTO.getDiffList().stream().map(FinancePremiumPolicy::getId).collect(Collectors.toList()))).build();
    }

    @Bean
    public FlatFileHeaderCallback thirdPartStepHeaderCallback(){
        return writer -> writer.write("订单号,金额,错误原因");
    }

    @Bean
    public FlatFileHeaderCallback thirdPartStepDiffHeaderCallback(){
        return writer -> writer.write("保司订单号,保司金额,微易订单号,微易金额,差异原因");
    }

    @Bean
    public ThirdPartStepWriterListener thirdPartStepWriterListener(FinancePremiumPolicyRepository financePremiumPolicyRepository){
        return new ThirdPartStepWriterListener(financePremiumPolicyRepository);
    }


    @Bean
    public Step thirdPartStep(FlatFileItemReader<FlatFileDTO> reader, ThirdPartStepProcessor thirdPartStepProcessor, ClassifierCompositeItemWriter<ReconDTO> classifierCompositeItemWriter, ThirdPartStepWriterListener thirdPartStepWriterListener) throws MalformedURLException {
        Step step = stepBuilderFactory.get("companyStep")
                .<FlatFileDTO, ReconDTO> chunk(10)
                .reader(reader)
                .processor(thirdPartStepProcessor)
                .writer(classifierCompositeItemWriter)
                .stream(thirdPartErrorLogWriter())
                .stream(thirdPartDiffLogWriter())
                .listener(thirdPartStepWriterListener)
                .build();
        return step;
    }

}
