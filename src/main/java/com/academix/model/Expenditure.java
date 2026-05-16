package com.academix.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.academix.model.enums.ExpenseCategory;

@Entity
@Table(name = "expenditure")
@Getter @Setter @NoArgsConstructor
public class Expenditure {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpenseCategory category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "voucher_number", length = 50)
    private String voucherNumber;

    @Column(name = "paid_to", length = 100)
    private String paidTo;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
