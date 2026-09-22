package com.example.livraison.mapper;

import com.example.livraison.dto.response.IncidentResponse;
import com.example.livraison.entity.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .type(incident.getType())
                .date(incident.getDate())
                .description(incident.getDescription())
                .commandeId(incident.getCommande().getId())
                .numeroCommande(incident.getCommande().getNumero())
                .livreurId(incident.getLivreur() != null ? incident.getLivreur().getId() : null)
                .nomLivreur(incident.getLivreur() != null ? incident.getLivreur().getNom() : null)
                .resolution(incident.getResolution())
                .build();
    }
}
