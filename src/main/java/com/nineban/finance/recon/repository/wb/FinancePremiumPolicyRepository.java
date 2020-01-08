package com.nineban.finance.recon.repository.wb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface FinancePremiumPolicyRepository extends JpaRepository<FinancePremiumPolicy,Long> {

    List<FinancePremiumPolicy> findByPolicySnIn(List<String> policySnList);

    /**
     * 重置订单状态为初始状态，相当于未对账
     * @param cooperateOrgId
     * @param status
     */
    @Transactional
    @Query(value = "update finance_premium_policy set batch_id=null,status=0 where cooperate_org_id=:cooperateOrgId and status=:status",nativeQuery = true)
    @Modifying
    void resetByCooperateOrgIdAndStatus(@Param("cooperateOrgId") Long cooperateOrgId,@Param("status") Integer status);

    /**
     * 如果对账成功，设置对账结果为一致（批次id不为空）的订单的状态为对账完成.
     * @param cooperateOrgId
     */
    @Transactional
    @Query(value = "update finance_premium_policy set status=2 where cooperate_org_id=:cooperateOrgId and status=1 and batch_id is not null",nativeQuery = true)
    @Modifying
    void completeByCooperateOrgIdAndStatus(@Param("cooperateOrgId") Long cooperateOrgId);

    /**
     * 如果对账成功，设置对账结果不一致（批次id为空）的订单状态为初始化状态。
     * @param cooperateOrgId
     */
    @Transactional
    @Query(value = "update finance_premium_policy set status=0 where cooperate_org_id=:cooperateOrgId and status=1 and batch_id is null",nativeQuery = true)
    @Modifying
    void unsetByCooperateOrgIdAndStatus(@Param("cooperateOrgId") Long cooperateOrgId);

    Page<FinancePremiumPolicy> findByCooperateOrgIdAndTransTimeBetweenAndStatus(Long cooperateOrgId, LocalDateTime transTimeStart, LocalDateTime transTimeEnd, Integer status,Pageable pageable);
}
