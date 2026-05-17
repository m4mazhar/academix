package com.academix.controller.admin;

import com.academix.service.BranchService;
import com.academix.service.UserService;
import com.academix.model.Branch;
import com.academix.model.User;
import com.academix.model.UserBranchRole;
import com.academix.model.enums.BranchRole;
import com.academix.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/branches")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class BranchAdminController {

    private final BranchService branchService;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("branches", branchService.findAll());
        model.addAttribute("pageTitle", "Branch Management");
        model.addAttribute("activeMenu", "branches");
        return "admin/branches/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("branch", new Branch());
        model.addAttribute("pageTitle", "New Branch");
        model.addAttribute("activeMenu", "branches");
        return "admin/branches/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Branch branch, RedirectAttributes ra) {
        branchService.save(branch);
        ra.addFlashAttribute("success", "Branch saved successfully.");
        return "redirect:/admin/branches";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("branch", branchService.findById(id));
        model.addAttribute("pageTitle", "Edit Branch");
        model.addAttribute("activeMenu", "branches");
        return "admin/branches/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Branch branch, RedirectAttributes ra) {
        branch.setId(id);
        branchService.save(branch);
        ra.addFlashAttribute("success", "Branch updated.");
        return "redirect:/admin/branches";
    }

    @GetMapping("/{id}/users")
    public String users(@PathVariable Long id, Model model) {
        Branch branch = branchService.findById(id);
        java.util.List<User> allUsers = userService.findAll();
        java.util.List<UserBranchRole> branchUsers = allUsers.stream()
            .flatMap(u -> u.getBranchRoles().stream())
            .filter(ubr -> ubr.getBranch() != null && ubr.getBranch().getId().equals(id))
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("branch", branch);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("branchUsers", branchUsers);
        model.addAttribute("roles", BranchRole.values());
        model.addAttribute("pageTitle", "Branch Users");
        model.addAttribute("activeMenu", "branches");
        return "admin/branches/users";
    }

    @PostMapping("/{id}/users")
    public String assignUser(@PathVariable Long id,
                             @RequestParam Long userId,
                             @RequestParam BranchRole role,
                             RedirectAttributes ra) {
        User user = userService.findById(userId);
        Branch branch = branchService.findById(id);
        UserBranchRole ubr = new UserBranchRole();
        ubr.setUser(user);
        ubr.setBranch(branch);
        ubr.setRole(role);
        user.getBranchRoles().add(ubr);
        userService.save(user, false);
        ra.addFlashAttribute("success", "User assigned to branch.");
        return "redirect:/admin/branches/" + id + "/users";
    }
}
