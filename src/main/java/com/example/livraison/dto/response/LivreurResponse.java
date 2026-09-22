package com.example.livraison.dto.response;

import com.example.livraison.entity.enums.StatutLivreur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivreurResponse {
    private Long id;
    private String nom;
    private String telephone;
    private String vehicule;
    private String zoneActivite;
    private StatutLivreur statut;
    private Double capaciteMaximale;
    private int chargeActuelle;
}
