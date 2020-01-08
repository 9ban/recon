package com.nineban.finance.recon.config;

import com.nineban.finance.recon.ContextContant;
import com.nineban.finance.recon.dto.SummaryDTO;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicy;
import com.nineban.finance.recon.repository.wb.FinancePremiumPolicyRepository;
import com.nineban.finance.recon.repository.wb.FinancePremiumSummary;
import com.nineban.finance.recon.repository.wb.FinancePremiumSummaryRepository;
import com.nineban.finance.recon.util.OSSUtil;
import lombok.SneakyThrows;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class JobListener implements JobExecutionListener {

    private final FinancePremiumSummaryRepository financePremiumSummaryRepository;

    private final FinancePremiumPolicyRepository financePremiumPolicyRepository;

    private final OSSUtil ossUtil;

    public JobListener(FinancePremiumSummaryRepository financePremiumSummaryRepository, FinancePremiumPolicyRepository financePremiumPolicyRepository, OSSUtil ossUtil) {
        this.financePremiumSummaryRepository = financePremiumSummaryRepository;
        this.financePremiumPolicyRepository = financePremiumPolicyRepository;
        this.ossUtil = ossUtil;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @SneakyThrows
    @Override
    public void afterJob(JobExecution jobExecution) {
        ExecutionContext executionContext = jobExecution.getExecutionContext();
        Long batchId = jobExecution.getJobParameters().getLong(ContextContant.BATCH_ID);
        Long cooperateOrgId = jobExecution.getJobParameters().getLong(ContextContant.COOPERATE_ORG_ID);
        FinancePremiumSummary financePremiumSummary = new FinancePremiumSummary();
        financePremiumSummary.setBatchId(batchId);
        //如果excel文件内容错误，上传错误文件内容
        if((boolean)executionContext.get(ContextContant.COMPANY_STEP_READ_ERROR)){
            financePremiumPolicyRepository.resetByCooperateOrgIdAndStatus(cooperateOrgId, FinancePremiumPolicy.STATUS_ING);
            InputStream inputStream = new FileSystemResource(ContextContant.ERROR_LOG_PATH).getInputStream();
            String fileName = ContextContant.ERROR_LOG_PATH.substring(0,ContextContant.ERROR_LOG_PATH.indexOf(".csv"))+System.currentTimeMillis();
            String fileUrl = ossUtil.uploadFile("abc",fileName+".csv",inputStream);
            financePremiumSummary.setErrorUrl(fileUrl);
            financePremiumSummaryRepository.save(financePremiumSummary);
        }else  {
            SummaryDTO summaryDTO = (SummaryDTO)executionContext.get(ContextContant.SUMMARY_KEY);
            if(summaryDTO.getThirdPartPlusCount()>0||summaryDTO.getMinePlusCount()>0||summaryDTO.getDiffCount()>0){
                InputStream inputStream = new FileSystemResource(ContextContant.DIFF_LOG_PATH).getInputStream();
                String fileName = ContextContant.DIFF_LOG_PATH.substring(0,ContextContant.DIFF_LOG_PATH.indexOf(".csv"))+System.currentTimeMillis();
                String fileUrl = ossUtil.uploadFile("abc",fileName+".csv",inputStream);
                financePremiumSummary.setErrorUrl(fileUrl);
            }
            BeanUtils.copyProperties(summaryDTO,financePremiumSummary);
            //如果存在金额不一致的，对账失败，
            if((boolean)executionContext.get(ContextContant.AMOUNT_DIFF)){
                financePremiumPolicyRepository.resetByCooperateOrgIdAndStatus(cooperateOrgId,FinancePremiumPolicy.STATUS_WAITING);
            }else{
                financePremiumPolicyRepository.completeByCooperateOrgIdAndStatus(cooperateOrgId);
                financePremiumPolicyRepository.unsetByCooperateOrgIdAndStatus(cooperateOrgId);
            }
            financePremiumSummaryRepository.save(financePremiumSummary);
        }
    }
}
