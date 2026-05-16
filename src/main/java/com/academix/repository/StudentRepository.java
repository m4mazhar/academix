package com.academix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.academix.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long>,
        JpaSpecificationExecutor<Student> {

    long countByBranchIdAndStatus(Long branchId, com.academix.model.enums.StudentStatus status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.studentCode, 3) AS int)), 0) FROM Student s WHERE s.branch.id = :branchId")
    int findMaxStudentSequence(Long branchId);
}
