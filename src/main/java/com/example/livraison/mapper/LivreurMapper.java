package com.example.livraison.mapper;

import com.example.livraison.dto.request.LivreurRequest;
import com.example.livraison.dto.response.LivreurResponse;
import com.example.livraison.entity.Livreur;
import org.springframework.stereotype.Component;

@Component
public class LivreurMapper {

    public Livreur toEntity(LivreurRequest req) {
        return Livreur.builder()
                .nom(req.getNom())
                .telephone(req.getTelephone())
                .vehicule(req.getVehicule())
                .zoneActivite(req.getZoneActivite())
                .statut(req.getStatut())
                .capaciteMaximale(req.getCapaciteMaximale())
                .build();
    }

    public void updateEntity(Livreur livreur, LivreurRequest req) {
        livreur.setNom(req.getNom());
        livreur.setTelephone(req.getTelephone());
        livreur.setVehicule(req.getVehicule());
        livreur.setZoneActivite(req.getZoneActivite());
        livreur.setStatut(req.getStatut());
        livreur.setCapaciteMaximale(req.getCapaciteMaximale());
    }

    public LivreurResponse toResponse(Livreur livreur, int chargeActuelle) {
        return LivreurResponse.builder()
                .id(livreur.getId())
                .nom(livreur.getNom())
                .telephone(livreur.getTelephone())
                .vehicule(livreur.getVehicule())
                .zoneActivite(livreur.getZoneActivite())
                .statut(livreur.getStatut())
                .capaciteMaximale(livreur.getCapaciteMaximale())
                .chargeActuelle(chargeActuelle)
                .build();
    }
}
