package com.example.livraison.controller.rest;

import com.example.livraison.dto.response.StatistiquesResponse;
import com.example.livraison.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class StatistiquesRestController {

    private final StatistiquesService statistiquesService;

    @GetMapping("/api/statistics/deliveries")
    public StatistiquesResponse livraisons(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) Long livreurId,
            @RequestParam(required = false) String zone) {
        return statistiquesService.calculer(debut, fin, livreurId, zone);
    }

    @GetMapping("/api/statistics/revenue")
    public StatistiquesResponse revenu(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) Long livreurId,
            @RequestParam(required = false) String zone) {
        return statistiquesService.calculer(debut, fin, livreurId, zone);
    }
}
