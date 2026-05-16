package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.BatchService;
import com.academix.service.FeeService;
import com.academix.service.StudentService;
import com.academix.dto.FeePaymentDto;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;
    private final StudentService studentService;
    private final BatchService batchService;
    private final BranchContext branchContext;

    @GetMapping
    public String list(@RequestParam(required = false) Long batchId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Integer month,
                       @RequestParam(required = false) Integer year,
                       Model model) {
        Long bid = branchContext.getActiveBranchId();
        model.addAttribute("fees", feeService.search(batchId, status, month, bid));
        model.addAttribute("batches", batchService.findAllActive(bid));
        model.addAttribute("pageTitle", "Student Fees");
        model.addAttribute("activeMenu", "fees");
        return "fee/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Long bid = branchContext.getActiveBranchId();
        model.addAttribute("fee", new FeePaymentDto());
        model.addAttribute("students", studentService.search(null, null, "ACTIVE"));
        model.addAttribute("batches", batchService.findAllActive(bid));
        model.addAttribute("pageTitle", "Record Payment");
        model.addAttribute("activeMenu", "fees");
        return "fee/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("fee") FeePaymentDto dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            Long bid = branchContext.getActiveBranchId();
            model.addAttribute("students", studentService.search(null, null, "ACTIVE"));
            model.addAttribute("batches", batchService.findAllActive(bid));
            return "fee/form";
        }
        var saved = feeService.save(dto, branchContext.getActiveBranchId());
        ra.addFlashAttribute("success", "Payment recorded. Receipt#: " + saved.getReceiptNumber());
        return "redirect:/fees";
    }

    @GetMapping("/receipt/{id}")
    public String viewReceipt(@PathVariable Long id, Model model) {
        model.addAttribute("fee", feeService.findById(id));
        model.addAttribute("pageTitle", "Receipt");
        return "fee/receipt";
    }
}
