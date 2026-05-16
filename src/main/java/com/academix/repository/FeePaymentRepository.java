package com.academix.repository;

import com.academix.model.FeePayment;
import com.academix.model.enums.FeeStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long>,
        JpaSpecificationExecutor<FeePayment> {

    @Query("SELECT COALESCE(SUM(f.amount),0) FROM FeePayment f WHERE f.branch.id = :branchId AND f.status = 'PAID' AND f.month = :month AND f.year = :year")
    BigDecimal sumCollectedByBranchAndMonthYear(Long branchId, int month, int year);

    @Query("SELECT COALESCE(SUM(f.amount),0) FROM FeePayment f WHERE f.status = 'PAID' AND f.month = :month AND f.year = :year")
    BigDecimal sumCollectedAllBranchesMonthYear(int month, int year);

    List<FeePayment> findByBranchIdAndStatus(Long branchId, FeeStatus status);

    @Query(value = "SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(receipt_number, '^.*-', '') AS INTEGER)), 0) FROM fee_payment WHERE branch_id = :branchId", nativeQuery = true)
    int findMaxReceiptSequence(Long branchId);
}
