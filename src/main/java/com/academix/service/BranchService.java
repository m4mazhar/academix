package com.academix.service;

import com.academix.exception.ResourceNotFoundException;
import com.academix.model.Branch;
import com.academix.repository.BranchRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<Branch> findAll() {
        return branchRepository.findAll();
    }

    public Branch findById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + id));
    }

    public Branch save(Branch branch) {
        return branchRepository.save(branch);
    }

    public void delete(Long id) {
        branchRepository.deleteById(id);
    }
}
