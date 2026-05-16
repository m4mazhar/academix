package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.BranchService;
import com.academix.service.TeacherService;
import com.academix.model.Teacher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final BranchService branchService;
    private final BranchContext branchContext;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) String status,
                       Model model) {
        model.addAttribute("teachers", teacherService.search(name, status));
        model.addAttribute("pageTitle", "Teachers");
        model.addAttribute("activeMenu", "teachers");
        return "teacher/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        model.addAttribute("pageTitle", "New Teacher");
        model.addAttribute("activeMenu", "teachers");
        return "teacher/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Teacher teacher, RedirectAttributes ra) {
        teacher.setBranch(branchService.findById(branchContext.getActiveBranchId()));
        teacherService.save(teacher);
        ra.addFlashAttribute("success", "Teacher saved successfully.");
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("teacher", teacherService.findById(id));
        model.addAttribute("pageTitle", "Edit Teacher");
        model.addAttribute("activeMenu", "teachers");
        return "teacher/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Teacher teacher, RedirectAttributes ra) {
        teacher.setId(id);
        teacherService.save(teacher);
        ra.addFlashAttribute("success", "Teacher updated successfully.");
        return "redirect:/teachers";
    }
}
