package com.academix.service;

import com.academix.dto.BranchSummaryDto;
import com.academix.model.Branch;
import com.academix.model.enums.BatchStatus;
import com.academix.model.enums.StudentStatus;
import com.academix.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final BranchRepository branchRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final TeacherRepository teacherRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final ExpenditureRepository expenditureRepository;

    public List<BranchSummaryDto> getCrossBranchSummary(Integer month, Integer year) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year  != null ? year  : LocalDate.now().getYear();

        return branchRepository.findAll().stream().map(branch -> {
            Long bid = branch.getId();
            BigDecimal collected = feePaymentRepository.sumCollectedByBranchAndMonthYear(bid, m, y);
            BigDecimal expended  = expenditureRepository.sumByBranchAndMonthYear(bid, m, y);
            return BranchSummaryDto.builder()
                    .branchId(bid)
                    .branchName(branch.getBranchName())
                    .branchCode(branch.getBranchCode())
                    .totalStudents(studentRepository.countByBranchIdAndStatus(bid, StudentStatus.ACTIVE))
                    .activeBatches(batchRepository.countByBranchIdAndStatus(bid, BatchStatus.ACTIVE))
                    .feeCollected(collected)
                    .totalExpenditure(expended)
                    .netBalance(collected.subtract(expended))
                    .build();
        }).collect(Collectors.toList());
    }

    public BranchSummaryDto getFinancialSummary(int month, int year, Long branchId) {
        int m = month > 0 ? month : LocalDate.now().getMonthValue();
        int y = year  > 0 ? year  : LocalDate.now().getYear();
        BigDecimal collected = branchId != null
                ? feePaymentRepository.sumCollectedByBranchAndMonthYear(branchId, m, y)
                : feePaymentRepository.sumCollectedAllBranchesMonthYear(m, y);
        BigDecimal expended = branchId != null
                ? expenditureRepository.sumByBranchAndMonthYear(branchId, m, y)
                : BigDecimal.ZERO;
        return BranchSummaryDto.builder()
                .feeCollected(collected)
                .totalExpenditure(expended)
                .netBalance(collected.subtract(expended))
                .build();
    }
}
