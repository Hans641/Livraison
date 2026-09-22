package com.example.livraison.dto.response;

import com.example.livraison.entity.enums.TypeIncident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private Long id;
    private TypeIncident type;
    private LocalDateTime date;
    private String description;
    private Long commandeId;
    private String numeroCommande;
    private Long livreurId;
    private String nomLivreur;
    private String resolution;
}
