package com.example.livraison.service.impl;

import com.example.livraison.entity.Commande;
import com.example.livraison.entity.enums.StatutCommande;
import com.example.livraison.dto.response.StatistiquesResponse;
import com.example.livraison.repository.CommandeRepository;
import com.example.livraison.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatistiquesServiceImpl implements StatistiquesService {

    private final CommandeRepository commandeRepository;

    @Override
    public StatistiquesResponse calculer(LocalDate debut, LocalDate fin, Long livreurId, String zone) {
        LocalDateTime dDebut = debut != null ? debut.atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime dFin = fin != null ? fin.atTime(23, 59, 59) : LocalDateTime.MAX;

        List<Commande> commandes = commandeRepository.findAll().stream()
                .filter(c -> !c.getDateCreation().isBefore(dDebut) && !c.getDateCreation().isAfter(dFin))
                .filter(c -> livreurId == null || (c.getLivreur() != null && c.getLivreur().getId().equals(livreurId)))
                .filter(c -> zone == null || zone.isBlank() || c.getZone().equalsIgnoreCase(zone))
                .toList();

        long total = commandes.size();
        long livrees = commandes.stream().filter(c -> c.getStatut() == StatutCommande.LIVREE).count();
        long echecs = commandes.stream().filter(c ->
                c.getStatut() == StatutCommande.ECHEC_LIVRAISON || c.getStatut() == StatutCommande.RETOURNEE).count();

        double chiffreAffaires = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.LIVREE)
                .mapToDouble(c -> c.getPrix() != null ? c.getPrix() : 0.0)
                .sum();

        double distanceTotale = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.LIVREE)
                .mapToDouble(Commande::getDistance)
                .sum();

        double delaiMoyenHeures = commandes.stream()
                .filter(c -> c.getStatut() == StatutCommande.LIVREE && c.getDateLivraisonEffective() != null)
                .mapToDouble(c -> Duration.between(c.getDateCreation(), c.getDateLivraisonEffective()).toMinutes() / 60.0)
                .average()
                .orElse(0.0);

        return StatistiquesResponse.builder()
                .nombreLivraisons(livrees)
                .tauxReussite(total == 0 ? 0 : Math.round((livrees * 10000.0 / total)) / 100.0)
                .tauxEchec(total == 0 ? 0 : Math.round((echecs * 10000.0 / total)) / 100.0)
                .delaiMoyenHeures(Math.round(delaiMoyenHeures * 100.0) / 100.0)
                .chiffreAffaires(Math.round(chiffreAffaires * 100.0) / 100.0)
                .revenuMoyenParLivraison(livrees == 0 ? 0 : Math.round((chiffreAffaires / livrees) * 100.0) / 100.0)
                .distanceTotaleParcourue(Math.round(distanceTotale * 100.0) / 100.0)
                .build();
    }
}
