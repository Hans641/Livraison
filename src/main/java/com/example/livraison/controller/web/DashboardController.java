package com.example.livraison.controller.web;

import com.example.livraison.service.CommandeService;
import com.example.livraison.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CommandeService commandeService;
    private final StatistiquesService statistiquesService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("commandesRecentes", commandeService.lister(null, null, PageRequest.of(0, 5)).getContent());
        model.addAttribute("enRetard", commandeService.listerEnRetard());
        model.addAttribute("stats", statistiquesService.calculer(null, null, null, null));
        return "dashboard";
    }
}
