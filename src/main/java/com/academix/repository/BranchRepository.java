package com.academix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academix.model.Branch;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long>,
        JpaSpecificationExecutor<Branch> {
    Optional<Branch> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);
}
