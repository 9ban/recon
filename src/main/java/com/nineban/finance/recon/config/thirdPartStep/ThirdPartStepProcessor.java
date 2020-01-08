package com.nineban.finance.recon.config.thirdPartStep;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.dto.FlatFileDTO;
import com.nineban.finance.recon.dto.ReconDTO;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class ThirdPartStepProcessor implements ItemProcessor<FlatFileDTO, ReconDTO>{

    private ExecutionContext executionContext;

    private Long batchId;

    private Set<String> policySet = new HashSet();

    @BeforeStep
    public void beforeStep(StepExecution stepExecution)
    {
        this.executionContext = stepExecution.getJobExecution().getExecutionContext();
        batchId = stepExecution.getJobParameters().getLong(ContextContant.BATCH_ID);
        policySet = new HashSet<>();
        this.executionContext.put(ContextContant.COMPANY_STEP_READ_ERROR,false);
        this.executionContext.put(ContextContant.AMOUNT_DIFF,false);
    }

    @Override
    public ReconDTO process(FlatFileDTO flatFileDTO) throws Exception {
        ReconDTO reconDTO = new ReconDTO();
        reconDTO.setBatchId(batchId);
        //如果文件中的第三方订单号为空、保单号重复、金额为空、金额不为数字，表示文件错误
        if(StringUtils.isEmpty(flatFileDTO.getPolicySn())){
            reconDTO.setError("订单号为空");
        }else{
            if(!policySet.add(flatFileDTO.getPolicySn())){
                reconDTO.setError("保单号重复");
            }
            reconDTO.setPolicySn(flatFileDTO.getPolicySn());
        }
        reconDTO.setAmountString(flatFileDTO.getAmount());
        if(StringUtils.isEmpty(flatFileDTO.getAmount())){
            reconDTO.setError("保费金额为空");
        }else{
            try{
                BigDecimal amount = new BigDecimal(flatFileDTO.getAmount());
                reconDTO.setAmount(amount);
            }catch (Exception e){
                reconDTO.setError("保费金额格式不正确");
            }
        }
        if(!StringUtils.isEmpty(reconDTO.getError())){
            reconDTO.setCompareResult(ReconDTO.COMPARE_RESULT_ERROR);
            executionContext.put(ContextContant.COMPANY_STEP_READ_ERROR,true);
        }else{
            reconDTO.setCompareResult(ReconDTO.COMPARE_RESULT_THIRD_PART_PLUS);
        }
        return reconDTO;
    }

}
