package com.academix.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @Builder
public class DashboardStatsDto {
    private long totalStudents;
    private long activeBatches;
    private BigDecimal feeCollectedThisMonth;
    private BigDecimal pendingFees;
    private long totalTeachers;
}
