package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.Batch;
import com.academix.model.enums.BatchStatus;
import com.academix.repository.BatchRepository;
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
public class BatchService {

    private final BatchRepository batchRepository;
    private final BranchContext branchContext;

    public List<Batch> findAll() {
        Long branchId = branchContext.getActiveBranchId();
        return batchRepository.findAll(BranchSpec.<Batch>inBranch(branchId), Sort.by("batchName"));
    }

    public List<Batch> findAllActive(Long branchId) {
        return batchRepository.findByBranchIdAndStatus(branchId, BatchStatus.ACTIVE);
    }

    public long countActive(Long branchId) {
        return batchRepository.countByBranchIdAndStatus(branchId, BatchStatus.ACTIVE);
    }

    public Batch findById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + id));
    }

    public Batch save(Batch batch) {
        return batchRepository.save(batch);
    }

    public void delete(Long id) {
        batchRepository.deleteById(id);
    }
}
