package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.PayrollEntry;
import com.academix.model.Teacher;
import com.academix.model.enums.PayrollStatus;
import com.academix.repository.PayrollRepository;
import com.academix.repository.TeacherRepository;
import com.academix.specification.BranchSpec;
import com.academix.specification.TeacherSpec;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PayrollService {

    private final TeacherRepository teacherRepository;
    private final PayrollRepository payrollRepository;
    private final BranchContext branchContext;

    public List<PayrollEntry> findByMonthYear(int month, int year) {
        Long branchId = branchContext.getActiveBranchId();
        return payrollRepository.findByBranchIdAndMonthAndYear(branchId, month, year);
    }

    /** Run payroll for all active teachers in the active branch */
    public List<PayrollEntry> runPayroll(int month, int year) {
        Long branchId = branchContext.getActiveBranchId();
        List<Teacher> teachers = teacherRepository.findAll(
            Specification.where(BranchSpec.<Teacher>inBranch(branchId))
                .and(TeacherSpec.isActive())
        );

        return teachers.stream()
            .filter(t -> !payrollRepository.existsByTeacherIdAndMonthAndYear(t.getId(), month, year))
            .map(teacher -> {
                PayrollEntry entry = new PayrollEntry();
                entry.setTeacher(teacher);
                entry.setBranch(teacher.getBranch());
                entry.setMonth(month);
                entry.setYear(year);
                entry.setBaseSalary(teacher.getBaseSalary());
                entry.setBonus(BigDecimal.ZERO);
                entry.setDeduction(BigDecimal.ZERO);
                entry.setNetPay(teacher.getBaseSalary());
                entry.setStatus(PayrollStatus.PENDING);
                return payrollRepository.save(entry);
            })
            .collect(Collectors.toList());
    }

    public PayrollEntry disburse(Long entryId) {
        PayrollEntry entry = payrollRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll entry not found: " + entryId));
        entry.setStatus(PayrollStatus.DISBURSED);
        entry.setDisbursedDate(LocalDate.now());
        return payrollRepository.save(entry);
    }

    public PayrollEntry update(PayrollEntry entry) {
        entry.setNetPay(entry.getBaseSalary().add(entry.getBonus()).subtract(entry.getDeduction()));
        return payrollRepository.save(entry);
    }
}
