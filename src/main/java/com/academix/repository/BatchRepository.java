package com.academix.repository;

import com.academix.model.Batch;
import com.academix.model.enums.BatchStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long>,
        JpaSpecificationExecutor<Batch> {

    List<Batch> findByBranchIdAndStatus(Long branchId, BatchStatus status);
    long countByBranchIdAndStatus(Long branchId, BatchStatus status);
}
