package com.example.livraison.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatistiquesResponse {
    private long nombreLivraisons;
    private double tauxReussite;
    private double tauxEchec;
    private double delaiMoyenHeures;
    private double chiffreAffaires;
    private double revenuMoyenParLivraison;
    private double distanceTotaleParcourue;
}
