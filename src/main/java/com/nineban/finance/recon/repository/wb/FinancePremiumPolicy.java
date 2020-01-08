package com.nineban.finance.recon.repository.wb;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "finance_premium_policy")
public class FinancePremiumPolicy {

    /**
     * 对账中
     */
    public static final Integer STATUS_ING = 1;

    /**
     * 未对账
     */
    public static final Integer STATUS_WAITING = 0;

    /**
     * 对账完成
     */
    public static final Integer STATUS_COMPLETE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单号
     */
    @Column(name = "policy_sn")
    private String policySn;

    /**
     * 订单金额
     */
    @Column
    private BigDecimal amount;

    /**
     * 批次号 外部传入
     */
    @Column(name = "batch_id")
    private Long batchId;

    /**
     * 状态 未对账，对账中，已对账
     */
    @Column
    private Integer status;

    /**
     * 第三方id
     */
    @Column(name = "cooperate_org_id")
    private Long cooperateOrgId;

    /**
     * 交易时间
     */
    @Column(name = "trans_time")
    private LocalDateTime transTime;
}
