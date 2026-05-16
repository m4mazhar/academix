package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.BranchService;
import com.academix.service.UserService;
import com.academix.model.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/branch")
@RequiredArgsConstructor
public class BranchSelectController {

    private final UserService userService;
    private final BranchService branchService;
    private final BranchContext branchContext;

    @GetMapping("/select")
    public String selectPage(Model model) {
        User current = userService.currentUser();
        model.addAttribute("branches", current.getAccessibleBranches());
        model.addAttribute("showAll", current.isSuperAdmin());
        model.addAttribute("pageTitle", "Select Branch");
        return "branch/select";
    }

    @PostMapping("/select")
    public String select(@RequestParam(required = false) String branchId) {
        Long id = (branchId == null || branchId.isBlank()) ? null : Long.parseLong(branchId);
        branchContext.setActiveBranchId(id);
        if (id != null) {
            branchContext.setActiveBranchName(
                branchService.findById(id).getBranchName()
            );
        } else {
            branchContext.setActiveBranchName("All Branches (HQ)");
        }
        return "redirect:/dashboard";
    }
}
