package com.academix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academix.model.Admission;

public interface AdmissionRepository extends JpaRepository<Admission, Long>,
        JpaSpecificationExecutor<Admission> {
}
