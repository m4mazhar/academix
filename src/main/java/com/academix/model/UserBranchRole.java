package com.academix.model;

import com.academix.model.enums.BranchRole;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_branch_role",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "branch_id", "role"}))
@Getter @Setter @NoArgsConstructor
public class UserBranchRole {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BranchRole role;
}
