package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.BatchService;
import com.academix.service.FeeService;
import com.academix.service.StudentService;
import com.academix.service.TeacherService;
import com.academix.dto.DashboardStatsDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final StudentService studentService;
    private final FeeService feeService;
    private final BatchService batchService;
    private final TeacherService teacherService;
    private final BranchContext branchContext;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Long bid = branchContext.getActiveBranchId();

        model.addAttribute("stats", DashboardStatsDto.builder()
            .totalStudents(studentService.countActive(bid))
            .activeBatches(batchService.countActive(bid))
            .feeCollectedThisMonth(feeService.totalCollectedThisMonth(bid))
            .pendingFees(feeService.totalPending(bid))
            .build());

        model.addAttribute("recentAdmissions", studentService.getRecentAdmissions(5, bid));
        model.addAttribute("overdueFees", feeService.getOverdueFees(bid));
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");
        return "dashboard/index";
    }
}
