package com.academix.controller;

import com.academix.context.BranchContext;
import com.academix.service.BatchService;
import com.academix.service.BranchService;
import com.academix.service.TeacherService;
import com.academix.model.Batch;
import com.academix.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final TeacherService teacherService;
    private final BranchService branchService;
    private final BranchContext branchContext;
    private final TeacherRepository teacherRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("batches", batchService.findAll());
        model.addAttribute("pageTitle", "Batches");
        model.addAttribute("activeMenu", "batches");
        return "batch/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("batch", new Batch());
        model.addAttribute("teachers", teacherService.search(null, "ACTIVE"));
        model.addAttribute("pageTitle", "New Batch");
        model.addAttribute("activeMenu", "batches");
        return "batch/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Batch batch,
                       @RequestParam(required = false) Long teacherId,
                       RedirectAttributes ra) {
        Long branchId = branchContext.getActiveBranchId();
        batch.setBranch(branchService.findById(branchId));
        if (teacherId != null) {
            batch.setTeacher(teacherRepository.findById(teacherId).orElse(null));
        }
        batchService.save(batch);
        ra.addFlashAttribute("success", "Batch saved successfully.");
        return "redirect:/batches";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("batch", batchService.findById(id));
        model.addAttribute("teachers", teacherService.search(null, "ACTIVE"));
        model.addAttribute("pageTitle", "Edit Batch");
        model.addAttribute("activeMenu", "batches");
        return "batch/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute Batch batch,
                         @RequestParam(required = false) Long teacherId,
                         RedirectAttributes ra) {
        batch.setId(id);
        if (teacherId != null) {
            batch.setTeacher(teacherRepository.findById(teacherId).orElse(null));
        }
        batchService.save(batch);
        ra.addFlashAttribute("success", "Batch updated successfully.");
        return "redirect:/batches";
    }
}
