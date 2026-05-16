package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.Expenditure;
import com.academix.repository.BranchRepository;
import com.academix.repository.ExpenditureRepository;
import com.academix.specification.BranchSpec;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final BranchRepository branchRepository;
    private final BranchContext branchContext;

    public List<Expenditure> findAll() {
        Long branchId = branchContext.getActiveBranchId();
        return expenditureRepository.findAll(
            BranchSpec.<Expenditure>inBranch(branchId),
            Sort.by(Sort.Direction.DESC, "date")
        );
    }

    public Expenditure findById(Long id) {
        return expenditureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure not found: " + id));
    }

    public Expenditure save(Expenditure expenditure) {
        Long branchId = branchContext.getActiveBranchId();
        if (expenditure.getBranch() == null) {
            expenditure.setBranch(branchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found")));
        }
        return expenditureRepository.save(expenditure);
    }

    public void delete(Long id) {
        expenditureRepository.deleteById(id);
    }
}
