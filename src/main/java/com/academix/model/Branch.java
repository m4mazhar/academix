package com.academix.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

import com.academix.model.enums.BranchStatus;

@Entity
@Table(name = "branch")
@Getter @Setter @NoArgsConstructor
public class Branch {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_code", nullable = false, unique = true, length = 20)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 100)
    private String branchName;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BranchStatus status = BranchStatus.ACTIVE;

    @Column(name = "opened_date")
    private LocalDate openedDate;

    @OneToMany(mappedBy = "branch")
    private List<Teacher> teachers;

    @OneToMany(mappedBy = "branch")
    private List<Batch> batches;
}
