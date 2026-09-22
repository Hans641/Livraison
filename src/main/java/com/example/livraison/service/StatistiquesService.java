package com.example.livraison.service;

import com.example.livraison.dto.response.StatistiquesResponse;

import java.time.LocalDate;

public interface StatistiquesService {
    StatistiquesResponse calculer(LocalDate debut, LocalDate fin, Long livreurId, String zone);
}
