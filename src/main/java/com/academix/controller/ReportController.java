package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.ReportService;
import com.academix.service.StudentService;
import com.academix.service.TeacherService;
import com.academix.service.FeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final BranchContext branchContext;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final FeeService feeService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Reports");
        model.addAttribute("activeMenu", "reports");
        return "report/index";
    }

    @GetMapping("/students")
    public String students(Model model) {
        model.addAttribute("students", studentService.search(null, null, null));
        model.addAttribute("pageTitle", "Student Report");
        model.addAttribute("activeMenu", "reports");
        return "report/students";
    }

    @GetMapping("/teachers")
    public String teachers(Model model) {
        model.addAttribute("teachers", teacherService.search(null, null));
        model.addAttribute("pageTitle", "Teacher Report");
        model.addAttribute("activeMenu", "reports");
        return "report/teachers";
    }

    @GetMapping("/fees")
    public String fees(@RequestParam(defaultValue = "0") int month,
                       @RequestParam(defaultValue = "0") int year,
                       Model model) {
        Long bid = branchContext.getActiveBranchId();
        model.addAttribute("fees", feeService.search(null, null, month > 0 ? month : null, bid));
        model.addAttribute("month", month);
        model.addAttribute("year", year);
        model.addAttribute("pageTitle", "Fee Collection Report");
        model.addAttribute("activeMenu", "reports");
        return "report/fees";
    }

    @GetMapping("/payroll")
    public String payroll(Model model) {
        model.addAttribute("pageTitle", "Payroll Report");
        model.addAttribute("activeMenu", "reports");
        return "report/index";  // redirect to index for now; full payroll report via /payroll
    }

    @GetMapping("/financial")
    public String financial(@RequestParam(defaultValue = "0") int month,
                            @RequestParam(defaultValue = "0") int year,
                            Model model) {
        int m = month > 0 ? month : LocalDate.now().getMonthValue();
        int y = year  > 0 ? year  : LocalDate.now().getYear();
        model.addAttribute("summary", reportService.getFinancialSummary(m, y, branchContext.getActiveBranchId()));
        model.addAttribute("month", m);
        model.addAttribute("year", y);
        model.addAttribute("pageTitle", "Financial Report");
        model.addAttribute("activeMenu", "reports");
        return "report/financial";
    }

    @GetMapping("/cross-branch")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String crossBranch(@RequestParam(required = false) Integer month,
                              @RequestParam(required = false) Integer year,
                              Model model) {
        model.addAttribute("summaries", reportService.getCrossBranchSummary(month, year));
        model.addAttribute("pageTitle", "Cross-Branch Report");
        model.addAttribute("activeMenu", "reports");
        return "report/cross-branch";
    }
}
