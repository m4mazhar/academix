package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.dto.FeePaymentDto;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.*;
import com.academix.model.enums.FeeStatus;
import com.academix.repository.*;
import com.academix.specification.BranchSpec;
import com.academix.specification.FeeSpec;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FeeService {

    private final FeePaymentRepository feePaymentRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final BranchRepository branchRepository;
    private final BranchContext branchContext;

    public List<FeePayment> search(Long batchId, String status, Integer month, Long branchId) {
        return feePaymentRepository.findAll(
            Specification.where(BranchSpec.<FeePayment>inBranch(branchId))
                .and(FeeSpec.inBatch(batchId))
                .and(FeeSpec.hasStatus(status))
                .and(FeeSpec.hasMonth(month))
        );
    }

    public BigDecimal totalCollectedThisMonth(Long branchId) {
        LocalDate now = LocalDate.now();
        if (branchId != null) {
            return feePaymentRepository.sumCollectedByBranchAndMonthYear(branchId, now.getMonthValue(), now.getYear());
        }
        return feePaymentRepository.sumCollectedAllBranchesMonthYear(now.getMonthValue(), now.getYear());
    }

    public BigDecimal totalPending(Long branchId) {
        if (branchId == null) return BigDecimal.ZERO;
        return feePaymentRepository.findByBranchIdAndStatus(branchId, FeeStatus.PENDING)
                .stream().map(FeePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<FeePayment> getOverdueFees(Long branchId) {
        if (branchId == null) return List.of();
        return feePaymentRepository.findByBranchIdAndStatus(branchId, FeeStatus.OVERDUE);
    }

    public FeePayment findById(Long id) {
        return feePaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee payment not found: " + id));
    }

    public FeePayment save(FeePaymentDto dto, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        FeePayment fee = new FeePayment();
        fee.setStudent(studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found")));
        fee.setBatch(batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found")));
        fee.setBranch(branch);
        fee.setMonth(dto.getMonth());
        fee.setYear(dto.getYear());
        fee.setAmount(dto.getAmount());
        fee.setPaymentMode(dto.getPaymentMode());
        fee.setStatus(FeeStatus.PAID);
        fee.setPaidDate(dto.getPaidDate() != null ? dto.getPaidDate() : LocalDate.now());
        fee.setNotes(dto.getNotes());

        int seq = feePaymentRepository.findMaxReceiptSequence(branchId) + 1;
        fee.setReceiptNumber(branch.getBranchCode() + "-R-" + String.format("%04d", seq));

        return feePaymentRepository.save(fee);
    }

    /** Auto-mark PENDING fees as OVERDUE after 30th of due month */
    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdueFees() {
        LocalDate threshold = LocalDate.now().minusMonths(1).withDayOfMonth(30);
        feePaymentRepository.findAll().stream()
            .filter(f -> f.getStatus() == FeeStatus.PENDING)
            .filter(f -> {
                LocalDate dueDate = LocalDate.of(f.getYear(), f.getMonth(), 30);
                return dueDate.isBefore(LocalDate.now());
            })
            .forEach(f -> {
                f.setStatus(FeeStatus.OVERDUE);
                feePaymentRepository.save(f);
            });
    }
}
