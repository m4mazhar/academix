package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.dto.StudentDto;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.Branch;
import com.academix.model.Student;
import com.academix.model.enums.StudentStatus;
import com.academix.repository.BatchRepository;
import com.academix.repository.BranchRepository;
import com.academix.repository.StudentRepository;
import com.academix.specification.BranchSpec;
import com.academix.specification.StudentSpec;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final BranchRepository branchRepository;
    private final BranchContext branchContext;

    public List<Student> search(String name, Long batchId, String status) {
        Long branchId = branchContext.getActiveBranchId();
        return studentRepository.findAll(
            Specification.where(BranchSpec.<Student>inBranch(branchId))
                .and(StudentSpec.nameLike(name))
                .and(StudentSpec.inBatch(batchId))
                .and(StudentSpec.hasStatus(status)),
            Sort.by("fullName")
        );
    }

    public long countActive(Long branchId) {
        return studentRepository.countByBranchIdAndStatus(branchId, StudentStatus.ACTIVE);
    }

    public List<Student> getRecentAdmissions(int limit, Long branchId) {
        return studentRepository.findAll(
            BranchSpec.<Student>inBranch(branchId),
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "admissionDate"))
        ).getContent();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    public Student save(StudentDto dto, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));

        Student student = dto.getId() != null
                ? findById(dto.getId())
                : new Student();

        student.setFullName(dto.getFullName());
        student.setPhone(dto.getPhone());
        student.setGuardianName(dto.getGuardianName());
        student.setGuardianPhone(dto.getGuardianPhone());
        student.setAddress(dto.getAddress());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setStatus(dto.getStatus() != null ? dto.getStatus() : StudentStatus.ACTIVE);
        student.setAdmissionDate(dto.getAdmissionDate() != null ? dto.getAdmissionDate() : LocalDate.now());
        student.setBranch(branch);
        student.setBatch(batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found")));

        if (student.getStudentCode() == null) {
            int seq = studentRepository.findMaxStudentSequence(branchId) + 1;
            student.setStudentCode(String.format("S-%03d", seq));
        }

        return studentRepository.save(student);
    }
}
