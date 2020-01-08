package com.nineban.finance.recon.config.thirdPartStep;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.dto.ReconDTO;
import com.nineban.finance.recon.dto.SummaryDTO;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicy;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicyRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.BeforeWrite;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThirdPartStepWriterListener {

    private final FinancePremiumPolicyRepository financePremiumPolicyRepository;

    private ExecutionContext executionContext;

    private SummaryDTO summaryDTO = new SummaryDTO();

    private Long batchId;

    public ThirdPartStepWriterListener(FinancePremiumPolicyRepository financePremiumPolicyRepository) {
        this.financePremiumPolicyRepository = financePremiumPolicyRepository;
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution){
        this.executionContext = stepExecution.getJobExecution().getExecutionContext();
        batchId = stepExecution.getJobParameters().getLong(ContextContant.BATCH_ID);
    }


    @BeforeWrite
    public void beforeWrite(List<? extends ReconDTO> reconDTOList) {
        //在写入之前，如果读取的文件没有错误，根据订单号批量取出订单，并比较第三方和我方数据,并且设置概览数据
        if(!(boolean)executionContext.get(ContextContant.COMPANY_STEP_READ_ERROR)){
            List<String> policySnList = reconDTOList.stream().map(ReconDTO::getPolicySn).collect(Collectors.toList());
            List<FinancePremiumPolicy> financePremiumPolicyList =  financePremiumPolicyRepository.findByPolicySnIn(policySnList);
            Map<String,List<FinancePremiumPolicy>> policyMap = financePremiumPolicyList.stream().collect(Collectors.groupingBy(FinancePremiumPolicy::getPolicySn));
            for (ReconDTO reconDTO : reconDTOList) {
                reconDTO.setPremiumPolicy(policyMap.get(reconDTO.getPolicySn()),batchId);
                summaryDTO.setThirdPartCount(summaryDTO.getThirdPartCount()+1);
                if(summaryDTO.getThirdPartAmount()!=null){
                    summaryDTO.setThirdPartAmount(reconDTO.getAmount().add(summaryDTO.getThirdPartAmount()));
                }else{
                    summaryDTO.setThirdPartAmount(reconDTO.getAmount());
                }
                //如果对比结果为既有相同又有不同，设置相同条数和相同金额，同时设置我方有对方无
                if(ReconDTO.COMPARE_RESULT_SAME_AND_DIFF.equals(reconDTO.getCompareResult())){
                    summaryDTO.setSameCount(summaryDTO.getSameCount()+1);
                    if(summaryDTO.getSameAmount()!=null){
                        summaryDTO.setSameAmount(summaryDTO.getSameAmount().add(reconDTO.getAmount()));
                    }else{
                        summaryDTO.setSameAmount(reconDTO.getAmount());
                    }
                    summaryDTO.setMinePlusCount(summaryDTO.getMinePlusCount()+1);
                    if(summaryDTO.getMinePlusAmount()!=null){
                        summaryDTO.setMinePlusAmount(summaryDTO.getMinePlusAmount().add(reconDTO.getDiffAmount()));
                    }else{
                        summaryDTO.setMinePlusAmount(reconDTO.getDiffAmount());
                    }
                    //如果对比一致，设置相同金额和条数
                }else if(ReconDTO.COMPARE_RESULT_SAME.equals(reconDTO.getCompareResult())){
                    summaryDTO.setSameCount(summaryDTO.getSameCount()+1);
                    if(summaryDTO.getSameAmount()!=null){
                        summaryDTO.setSameAmount(summaryDTO.getSameAmount().add(reconDTO.getAmount()));
                    }else{
                        summaryDTO.setSameAmount(reconDTO.getAmount());
                    }
                    //如果不一致，设置不同条数和差异金额
                }else if(ReconDTO.COMPARE_RESULT_DIFF.equals(reconDTO.getCompareResult())){
                    this.executionContext.put(ContextContant.AMOUNT_DIFF,true);
                    summaryDTO.setDiffCount(summaryDTO.getDiffCount()+1);
                    if(summaryDTO.getDiffAmount()!=null){
                        summaryDTO.setDiffAmount(reconDTO.getDiffAmount().add(summaryDTO.getDiffAmount()));
                    }else{
                        summaryDTO.setDiffAmount(reconDTO.getDiffAmount());
                    }
                }
                summaryDTO.setMineCount(summaryDTO.getMineCount()+reconDTO.getDiffList().size()+reconDTO.getSameList().size());
                BigDecimal mineAmount = reconDTO.getSameList().stream().map(FinancePremiumPolicy::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                mineAmount = mineAmount.add(reconDTO.getDiffList().stream().map(FinancePremiumPolicy::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
                summaryDTO.setMineAmount(summaryDTO.getMineAmount().add(mineAmount));
            }
            executionContext.put(ContextContant.SUMMARY_KEY,summaryDTO);
        }
    }
}
