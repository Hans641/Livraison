package com.example.livraison.controller.web;

import com.example.livraison.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class StatistiquesWebController {

    private final StatistiquesService statistiquesService;

    @GetMapping("/statistiques")
    public String statistiques(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) String zone,
            Model model) {
        model.addAttribute("stats", statistiquesService.calculer(debut, fin, null, zone));
        model.addAttribute("debut", debut);
        model.addAttribute("fin", fin);
        model.addAttribute("zone", zone);
        return "statistiques";
    }
}
