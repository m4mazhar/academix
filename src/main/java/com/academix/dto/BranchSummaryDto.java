package com.academix.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @Builder
public class BranchSummaryDto {
    private Long branchId;
    private String branchName;
    private String branchCode;
    private long totalStudents;
    private long activeBatches;
    private long totalTeachers;
    private BigDecimal feeCollected;
    private BigDecimal totalExpenditure;
    private BigDecimal netBalance;
}
