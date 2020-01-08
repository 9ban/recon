package com.nineban.finance.recon.config.wyStep;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.dto.ReconDTO;
import com.nineban.finance.recon.dto.SummaryDTO;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicy;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;

public class MineStepProcessor implements ItemProcessor<FinancePremiumPolicy, ReconDTO>{

    private ExecutionContext executionContext;

    private Long batchId;

    private SummaryDTO summaryDTO;


    @BeforeStep
    public void beforeStep(StepExecution stepExecution)
    {
        this.executionContext = stepExecution.getJobExecution().getExecutionContext();
        batchId = stepExecution.getJobParameters().getLong(ContextContant.BATCH_ID);
        summaryDTO = (SummaryDTO)executionContext.get(ContextContant.SUMMARY_KEY);
    }

    @Override
    public ReconDTO process(FinancePremiumPolicy financePremiumPolicy) throws Exception {
        ReconDTO reconDTO = new ReconDTO();
        reconDTO.setBatchId(batchId);
        reconDTO.setPolicySn(financePremiumPolicy.getPolicySn());
        reconDTO.setAmount(financePremiumPolicy.getAmount());
        reconDTO.setCompareResult(ReconDTO.COMPARE_RESULT_MINE_PLUS);
        summaryDTO.setMinePlusCount(summaryDTO.getMinePlusCount()+1);
        summaryDTO.setMineCount(summaryDTO.getMineCount()+1);
        summaryDTO.setMinePlusAmount(summaryDTO.getMinePlusAmount().add(financePremiumPolicy.getAmount()));
        summaryDTO.setMineAmount(summaryDTO.getMineAmount().add(financePremiumPolicy.getAmount()));
        return reconDTO;
    }

}
