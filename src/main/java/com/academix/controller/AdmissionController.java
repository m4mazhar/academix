package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.AdmissionService;
import com.academix.service.BatchService;
import com.academix.dto.AdmissionDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admissions")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;
    private final BatchService batchService;
    private final BranchContext branchContext;

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("admission", new AdmissionDto());
        model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
        model.addAttribute("pageTitle", "New Admission");
        model.addAttribute("activeMenu", "admission");
        return "admission/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("admission") AdmissionDto dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
            return "admission/form";
        }
        var admission = admissionService.admit(dto);
        ra.addFlashAttribute("success",
            "Admission successful! Admission #: " + admission.getAdmissionNumber());
        return "redirect:/students";
    }
}
