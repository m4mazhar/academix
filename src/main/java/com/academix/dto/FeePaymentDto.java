package com.academix.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import com.academix.model.enums.PaymentMode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class FeePaymentDto {

    @NotNull(message = "Student is required")
    private Long studentId;

    @NotNull(message = "Batch is required")
    private Long batchId;

    @NotNull(message = "Month is required")
    @Min(1) @Max(12)
    private Integer month;

    @NotNull(message = "Year is required")
    private Integer year;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    private PaymentMode paymentMode = PaymentMode.CASH;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate paidDate;

    private String notes;
}
