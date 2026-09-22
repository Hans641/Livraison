package com.example.livraison.mapper;

import com.example.livraison.dto.response.CommandeResponse;
import com.example.livraison.entity.Commande;
import com.example.livraison.entity.enums.StatutCommande;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommandeMapper {

    public CommandeResponse toResponse(Commande c) {
        boolean enRetard = c.getStatut() != StatutCommande.LIVREE
                && c.getStatut() != StatutCommande.ANNULEE
                && c.getDatePrevue() != null
                && c.getDatePrevue().isBefore(LocalDateTime.now());

        return CommandeResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .clientId(c.getClient().getId())
                .nomClient(c.getClient().getNom())
                .adresseEnlevement(c.getAdresseEnlevement())
                .adresseLivraison(c.getAdresseLivraison())
                .poids(c.getPoids())
                .volume(c.getVolume())
                .priorite(c.getPriorite())
                .distance(c.getDistance())
                .zone(c.getZone())
                .prix(c.getPrix())
                .datePrevue(c.getDatePrevue())
                .statut(c.getStatut())
                .livreurId(c.getLivreur() != null ? c.getLivreur().getId() : null)
                .nomLivreur(c.getLivreur() != null ? c.getLivreur().getNom() : null)
                .enRetard(enRetard)
                .build();
    }
}
