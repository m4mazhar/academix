package com.academix.config;

import com.academix.model.Branch;
import com.academix.model.User;
import com.academix.model.UserBranchRole;
import com.academix.model.enums.BranchRole;
import com.academix.model.enums.BranchStatus;
import com.academix.repository.BranchRepository;
import com.academix.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Seeds the database with an initial branch and super-admin user on first start.
 * Safe to run repeatedly — checks existence before inserting.
 *
 * Default credentials:  admin / admin123
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // ── 1. Default branch ────────────────────────────────────────
        Branch branch;
        if (branchRepository.count() == 0) {
            branch = new Branch();
            branch.setBranchCode("BR-HQ");
            branch.setBranchName("Main Branch");
            branch.setStatus(BranchStatus.ACTIVE);
            branch.setOpenedDate(LocalDate.now());
            branch = branchRepository.save(branch);
            log.info("DataInitializer: created default branch '{}'", branch.getBranchName());
        } else {
            branch = branchRepository.findAll().get(0);
        }

        // ── 2. Super-admin user ──────────────────────────────────────
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Admin");
            admin.setEnabled(true);

            UserBranchRole ubr = new UserBranchRole();
            ubr.setUser(admin);
            ubr.setBranch(branch);
            ubr.setRole(BranchRole.SUPER_ADMIN);

            admin.setBranchRoles(new ArrayList<>());
            admin.getBranchRoles().add(ubr);

            userRepository.save(admin);
            log.info("DataInitializer: created default admin user 'admin' (password: admin123)");
        }
    }
}
