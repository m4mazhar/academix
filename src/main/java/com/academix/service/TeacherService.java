package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.Teacher;
import com.academix.repository.TeacherRepository;
import com.academix.specification.BranchSpec;
import com.academix.specification.TeacherSpec;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final BranchContext branchContext;

    public List<Teacher> search(String name, String status) {
        Long branchId = branchContext.getActiveBranchId();
        return teacherRepository.findAll(
            Specification.where(BranchSpec.<Teacher>inBranch(branchId))
                .and(TeacherSpec.nameLike(name))
                .and(TeacherSpec.hasStatus(status)),
            Sort.by("fullName")
        );
    }

    public Teacher findById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + id));
    }

    public Teacher save(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }
}
