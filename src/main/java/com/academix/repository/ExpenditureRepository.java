package com.academix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.academix.model.Expenditure;

import java.math.BigDecimal;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long>,
        JpaSpecificationExecutor<Expenditure> {

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expenditure e WHERE e.branch.id = :branchId AND MONTH(e.date) = :month AND YEAR(e.date) = :year")
    BigDecimal sumByBranchAndMonthYear(Long branchId, int month, int year);
}
