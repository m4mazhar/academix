package com.academix.controller;

import com.academix.service.ExpenditureService;
import com.academix.model.Expenditure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expenditures")
@RequiredArgsConstructor
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("expenditures", expenditureService.findAll());
        model.addAttribute("pageTitle", "Expenditure");
        model.addAttribute("activeMenu", "expenditures");
        return "expenditure/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("expenditure", new Expenditure());
        model.addAttribute("categories", com.academix.model.enums.ExpenseCategory.values());
        model.addAttribute("pageTitle", "Add Expenditure");
        model.addAttribute("activeMenu", "expenditures");
        return "expenditure/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Expenditure expenditure, RedirectAttributes ra) {
        expenditureService.save(expenditure);
        ra.addFlashAttribute("success", "Expenditure saved successfully.");
        return "redirect:/expenditures";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("expenditure", expenditureService.findById(id));
        model.addAttribute("categories", com.academix.model.enums.ExpenseCategory.values());
        model.addAttribute("pageTitle", "Edit Expenditure");
        model.addAttribute("activeMenu", "expenditures");
        return "expenditure/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Expenditure expenditure, RedirectAttributes ra) {
        expenditure.setId(id);
        expenditureService.save(expenditure);
        ra.addFlashAttribute("success", "Expenditure updated.");
        return "redirect:/expenditures";
    }
}
