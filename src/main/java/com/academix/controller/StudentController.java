package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.BatchService;
import com.academix.service.StudentService;
import com.academix.dto.StudentDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final BatchService batchService;
    private final BranchContext branchContext;

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) Long batchId,
                       @RequestParam(required = false) String status,
                       Model model) {
        model.addAttribute("students", studentService.search(name, batchId, status));
        model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
        model.addAttribute("pageTitle", "Students");
        model.addAttribute("activeMenu", "students");
        return "student/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("student", new StudentDto());
        model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
        model.addAttribute("pageTitle", "New Student");
        model.addAttribute("activeMenu", "students");
        return "student/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("student") StudentDto dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
            model.addAttribute("pageTitle", "Student Form");
            return "student/form";
        }
        studentService.save(dto, branchContext.getActiveBranchId());
        ra.addFlashAttribute("success", "Student saved successfully.");
        return "redirect:/students";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        model.addAttribute("pageTitle", "Student Profile");
        model.addAttribute("activeMenu", "students");
        return "student/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var s = studentService.findById(id);
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setFullName(s.getFullName());
        dto.setPhone(s.getPhone());
        dto.setGuardianName(s.getGuardianName());
        dto.setGuardianPhone(s.getGuardianPhone());
        dto.setAddress(s.getAddress());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setBatchId(s.getBatch() != null ? s.getBatch().getId() : null);
        dto.setStatus(s.getStatus());
        dto.setAdmissionDate(s.getAdmissionDate());
        model.addAttribute("student", dto);
        model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
        model.addAttribute("pageTitle", "Edit Student");
        model.addAttribute("activeMenu", "students");
        return "student/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("student") StudentDto dto,
                         BindingResult result, Model model, RedirectAttributes ra) {
        dto.setId(id);
        if (result.hasErrors()) {
            model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
            return "student/form";
        }
        studentService.save(dto, branchContext.getActiveBranchId());
        ra.addFlashAttribute("success", "Student updated successfully.");
        return "redirect:/students/" + id;
    }
}
