package com.nineban.finance.recon.dto;

import com.nineban.finance.recon.repository.wb.FinancePremiumPolicy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class ReconDTO {

    /**
     * 对比结果 我方有第三方无
     */
    public static final Integer COMPARE_RESULT_MINE_PLUS = 1;
    /**
     * 对比结果 第三方有我方无
     */
    public static final Integer COMPARE_RESULT_THIRD_PART_PLUS = 2;

    /**
     * 对比结果 双方金额一致
     */
    public static final Integer COMPARE_RESULT_SAME = 3;

    /**
     * 对比结果 双方金额不一致
     */
    public static final Integer COMPARE_RESULT_DIFF = 4;

    /**
     * 对比结果 我方部分相加的金额等于第三方的
     */
    public static final Integer COMPARE_RESULT_SAME_AND_DIFF = 5;

    /**
     * 对比失败 三方给的数据有问题
     */
    public static final Integer COMPARE_RESULT_ERROR= 6;


    private Long batchId;

    /**
     * 订单号，用于区分是否同一单的字段
     */
    private String policySn;

    /**
     * 第三方的金额
     */
    private String amountString;

    /**
     * 如果第三方数据有问题，显示第三方错误内容
     */
    private String error;

    /**
     * 第三方的金额 数字类型
     */
    private BigDecimal amount;

    /**
     * 差异金额
     */
    private BigDecimal diffAmount;

    private Integer compareResult;

    private List<FinancePremiumPolicy> sameList;

    private List<FinancePremiumPolicy> diffList;

    /**
     * 对比我方订单和第三方订单
     * @param financePremiumPolicyList
     * @param batchId
     */
    public void setPremiumPolicy(List<FinancePremiumPolicy> financePremiumPolicyList,Long batchId){
        this.diffAmount = BigDecimal.ZERO;
        //如果不存在我方订单，对比结果为第三方有我方无
        if(CollectionUtils.isEmpty(financePremiumPolicyList)){
            compareResult = COMPARE_RESULT_THIRD_PART_PLUS;
            sameList = Collections.EMPTY_LIST;
            diffList = Collections.EMPTY_LIST;
        }else {
            //如果我方有对应订单号的订单，从我方订单中找出相加金额等于第三方金额的订单组合
            sameList = getMatchList(financePremiumPolicyList,amount,0);
            sameList.stream().forEach(item->{
                item.setStatus(FinancePremiumPolicy.STATUS_ING);
                item.setBatchId(batchId);
            });
            Set<Long> idSet = sameList.stream().map(FinancePremiumPolicy::getId).collect(Collectors.toSet());
            //排除相同列表里面的数据，剩下为不同列表
            diffList = financePremiumPolicyList.stream().filter(item->!idSet.contains(item.getId()))
                    .map(item->{
                        item.setStatus(FinancePremiumPolicy.STATUS_ING);
                        return item;
                    }).collect(Collectors.toList());
            //如果相同列表和不同列表同时不为空，表示我方有部分订单同第三方相同，还有部分为我方有对方无
            //如果相同列表不为空，不同列表为空，表示双方一致
            //如果相同列表为空，不同列表不为空，表示双方金额不一致
            if(!CollectionUtils.isEmpty(sameList)&&!CollectionUtils.isEmpty(diffList)){
                compareResult = COMPARE_RESULT_SAME_AND_DIFF;
                this.diffAmount = diffList.stream().map(FinancePremiumPolicy::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            }else if(CollectionUtils.isEmpty(diffList)){
                compareResult = COMPARE_RESULT_SAME;
            }else {
                compareResult = COMPARE_RESULT_DIFF;
            }
        }
    }

    private List<FinancePremiumPolicy> getMatchList(List<FinancePremiumPolicy> financePremiumPolicyList,BigDecimal targetAmount,int currentIndex){
        List<FinancePremiumPolicy> matchList = new ArrayList<>();
        for (int i = currentIndex; i < financePremiumPolicyList.size(); i++) {
            FinancePremiumPolicy financePremiumPolicy = financePremiumPolicyList.get(i);
            BigDecimal subTargetAmount = targetAmount.subtract(financePremiumPolicy.getAmount());
            if(subTargetAmount.compareTo(BigDecimal.ZERO)==0){
                matchList.add(financePremiumPolicy);
                break;
            }else{
                List<FinancePremiumPolicy> subMatchList = getMatchList(financePremiumPolicyList,subTargetAmount,i+1);
                if(!CollectionUtils.isEmpty(subMatchList)){
                    matchList.add(financePremiumPolicy);
                    matchList.addAll(subMatchList);
                    break;
                }
            }
        }
        return matchList;
    }

    public String getDiffMsg(){
        if(COMPARE_RESULT_MINE_PLUS.equals(getCompareResult())){
            return "我方有第三方无";
        }else if(COMPARE_RESULT_THIRD_PART_PLUS.equals(getCompareResult())){
            return "第三方有我方无";
        }else if(COMPARE_RESULT_DIFF.equals(getCompareResult())){
            return "金额不一致";
        }else if(COMPARE_RESULT_SAME_AND_DIFF.equals(getCompareResult())){
            //如果我方的金额和第三方的金额不一致，并且我方的部分组合等于第三方的金额，处理成我方部分和第三方一致，剩下部分为我方有第三方无
            return "我方有第三方无";
        }
        return "其他";
    }

    /**
     * 我方订单号，如果第三方有我方无，返回为空。其他返回保单号
     * @return
     */
    public String getDiffPolicySn(){
        if(COMPARE_RESULT_THIRD_PART_PLUS.equals(getCompareResult())){
            return null;
        }
        return policySn;
    }

    public String getPolicySn(){
        if(COMPARE_RESULT_MINE_PLUS.equals(getCompareResult())){
            return null;
        }
        return policySn;
    }
}
