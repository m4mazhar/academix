package com.academix.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import com.academix.model.enums.BranchRole;

@Entity
@Table(name = "app_user")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserBranchRole> branchRoles;

    public boolean isSuperAdmin() {
        return branchRoles != null && branchRoles.stream()
                .anyMatch(r -> r.getRole() == BranchRole.SUPER_ADMIN);
    }

    public List<Branch> getAccessibleBranches() {
        if (branchRoles == null) return List.of();
        return branchRoles.stream()
                .map(UserBranchRole::getBranch)
                .distinct()
                .collect(Collectors.toList());
    }
}
