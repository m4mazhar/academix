package com.academix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academix.model.PayrollEntry;

import java.util.List;

public interface PayrollRepository extends JpaRepository<PayrollEntry, Long>,
        JpaSpecificationExecutor<PayrollEntry> {

    List<PayrollEntry> findByBranchIdAndMonthAndYear(Long branchId, int month, int year);
    boolean existsByTeacherIdAndMonthAndYear(Long teacherId, int month, int year);
}
