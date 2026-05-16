package com.academix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.academix.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long>,
        JpaSpecificationExecutor<Teacher> {
}
