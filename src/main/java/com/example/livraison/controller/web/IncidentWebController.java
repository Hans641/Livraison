package com.example.livraison.controller.web;

import com.example.livraison.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/incidents")
@RequiredArgsConstructor
public class IncidentWebController {

    private final IncidentService incidentService;

    @GetMapping
    public String liste(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("incidents", incidentService.lister(PageRequest.of(page, 10)));
        return "incidents/list";
    }

    @PostMapping("/{id}/resoudre")
    public String resoudre(@PathVariable Long id, @RequestParam String resolution, RedirectAttributes redirectAttributes) {
        incidentService.resoudre(id, resolution);
        redirectAttributes.addFlashAttribute("succes", "Incident resolu");
        return "redirect:/incidents";
    }
}
