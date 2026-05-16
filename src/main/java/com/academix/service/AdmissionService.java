package com.academix.service;

import com.academix.context.BranchContext;
import com.academix.dto.AdmissionDto;
import com.academix.exception.ResourceNotFoundException;
import com.academix.model.*;
import com.academix.model.enums.FeeStatus;
import com.academix.model.enums.PaymentMode;
import com.academix.model.enums.StudentStatus;
import com.academix.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class AdmissionService {

    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final BranchRepository branchRepository;
    private final AdmissionRepository admissionRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final BranchContext branchContext;

    public Admission admit(AdmissionDto dto) {
        Long branchId = branchContext.getActiveBranchId();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        Batch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        // Create Student
        int seq = studentRepository.findMaxStudentSequence(branchId) + 1;
        Student student = new Student();
        student.setStudentCode(String.format("S-%03d", seq));
        student.setFullName(dto.getFullName());
        student.setPhone(dto.getPhone());
        student.setGuardianName(dto.getGuardianName());
        student.setGuardianPhone(dto.getGuardianPhone());
        student.setAddress(dto.getAddress());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setBatch(batch);
        student.setBranch(branch);
        student.setStatus(StudentStatus.ACTIVE);
        student.setAdmissionDate(dto.getAdmissionDate() != null ? dto.getAdmissionDate() : LocalDate.now());
        student = studentRepository.save(student);

        // Create Admission record
        Admission admission = new Admission();
        admission.setAdmissionNumber(branch.getBranchCode() + "-ADM-" + String.format("%04d", seq));
        admission.setStudent(student);
        admission.setBatch(batch);
        admission.setBranch(branch);
        admission.setAdmissionDate(student.getAdmissionDate());
        admission.setAdmissionFee(dto.getAdmissionFee());
        admission.setRemarks(dto.getRemarks());
        admission = admissionRepository.save(admission);

        // Create initial FeePayment for admission fee
        if (dto.getAdmissionFee() != null && dto.getAdmissionFee().compareTo(java.math.BigDecimal.ZERO) > 0) {
            int receiptSeq = feePaymentRepository.findMaxReceiptSequence(branchId) + 1;
            FeePayment fee = new FeePayment();
            fee.setReceiptNumber(branch.getBranchCode() + "-R-" + String.format("%04d", receiptSeq));
            fee.setStudent(student);
            fee.setBatch(batch);
            fee.setBranch(branch);
            fee.setAmount(dto.getAdmissionFee());
            fee.setPaymentMode(PaymentMode.CASH);
            fee.setStatus(FeeStatus.PAID);
            fee.setPaidDate(LocalDate.now());
            fee.setNotes("Admission fee");
            feePaymentRepository.save(fee);
        }

        return admission;
    }
}
