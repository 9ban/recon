package com.nineban.finance.recon.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SummaryDTO {

    /**
     * 我方保单总数
     */
    private int mineCount;

    /**
     * 我方总金额
     */
    private BigDecimal mineAmount = BigDecimal.ZERO;

    /**
     * 第三方总金额
     */
    private int thirdPartCount;

    /**
     * 第三方总金额
     */
    private BigDecimal thirdPartAmount = BigDecimal.ZERO;

    /**
     * 金额差异订单数
     */
    private int diffCount;

    /**
     * 金额差异订单差异总金额 为金额差异的第三方金额-我方金额
     */
    private BigDecimal diffAmount = BigDecimal.ZERO;

    /**
     * 我方有第三方无订单数
     */
    private int minePlusCount;

    /**
     * 我方有第三方无金额
     */
    private BigDecimal minePlusAmount = BigDecimal.ZERO;

    /**
     * 第三方有我方无订单数
     */
    private int thirdPartPlusCount;

    /**
     * 第三方有我方无金额数
     */
    private BigDecimal thirdPartPlusAmount = BigDecimal.ZERO;

    /**
     * 相同订单数
     */
    private int sameCount;

    /**
     * 相同金额
     */
    private BigDecimal sameAmount = BigDecimal.ZERO;

}
