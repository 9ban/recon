package com.nineban.finance.recon.repository.wb;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Table(name = "finance_premium_summary")
@Entity
public class FinancePremiumSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "mine_count")
    private int mineCount;

    @Column(name = "mine_amount")
    private BigDecimal mineAmount;

    @Column(name = "third_part_count")
    private int thirdPartCount;

    @Column(name = "third_part_amount")
    private BigDecimal thirdPartAmount;

    @Column(name = "diff_count")
    private int diffCount;

    @Column(name = "diff_amount")
    private BigDecimal diffAmount;

    @Column(name = "mine_plus_count")
    private int minePlusCount;

    @Column(name = "mine_plus_amount")
    private BigDecimal minePlusAmount;

    @Column(name = "third_part_plus_count")
    private int thirdPartPlusCount;

    @Column(name = "third_part_plus_amount")
    private BigDecimal thirdPartPlusAmount;

    @Column(name = "same_count")
    private int sameCount;

    @Column(name = "same_amount")
    private BigDecimal sameAmount;

    @Column(name = "error_url")
    private String errorUrl;

    @Column(name = "is_deleted")
    private boolean isDeleted;


}
