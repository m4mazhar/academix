package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;
    private final BranchContext branchContext;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int month,
                       @RequestParam(defaultValue = "0") int year,
                       Model model) {
        int m = month > 0 ? month : LocalDate.now().getMonthValue();
        int y = year  > 0 ? year  : LocalDate.now().getYear();
        model.addAttribute("entries", payrollService.findByMonthYear(m, y));
        model.addAttribute("month", m);
        model.addAttribute("year", y);
        model.addAttribute("pageTitle", "Payroll");
        model.addAttribute("activeMenu", "payroll");
        return "payroll/list";
    }

    @GetMapping("/run")
    public String runForm(Model model) {
        model.addAttribute("month", LocalDate.now().getMonthValue());
        model.addAttribute("year",  LocalDate.now().getYear());
        model.addAttribute("pageTitle", "Run Payroll");
        model.addAttribute("activeMenu", "payroll");
        return "payroll/run";
    }

    @PostMapping("/run")
    public String run(@RequestParam int month, @RequestParam int year, RedirectAttributes ra) {
        var entries = payrollService.runPayroll(month, year);
        ra.addFlashAttribute("success", "Payroll run complete. " + entries.size() + " entries created.");
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @PostMapping("/{id}/disburse")
    public String disburse(@PathVariable Long id, RedirectAttributes ra) {
        payrollService.disburse(id);
        ra.addFlashAttribute("success", "Payment marked as disbursed.");
        return "redirect:/payroll";
    }
}
