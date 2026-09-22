package com.example.livraison.util;

import com.example.livraison.entity.Commande;
import com.example.livraison.entity.Livreur;
import com.example.livraison.entity.enums.PrioriteCommande;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calcule un score d'eligibilite pour l'attribution automatique des livraisons.
 * Score plus eleve = candidat plus adapte.
 * Ponderation configurable (livraison.score.*).
 */
@Component
public class ScoreCalculator {

    @Value("${livraison.score.poidsDistance}")
    private double poidsDistance;

    @Value("${livraison.score.poidsCharge}")
    private double poidsCharge;

    @Value("${livraison.score.poidsPriorite}")
    private double poidsPriorite;

    public double score(Livreur livreur, Commande commande, int chargeActuelle) {
        // Bonus si le livreur est deja dans la meme zone
        double scoreZone = livreur.getZoneActivite().equalsIgnoreCase(commande.getZone()) ? 1.0 : 0.3;

        // Moins le livreur est charge, plus le score est haut
        double scoreCharge = 1.0 / (1.0 + chargeActuelle);

        // Priorite : les commandes urgentes favorisent les livreurs peu charges
        double bonusPriorite = commande.getPriorite() == PrioriteCommande.TRES_URGENTE ? 1.0
                : commande.getPriorite() == PrioriteCommande.URGENTE ? 0.6 : 0.3;

        return (scoreZone * poidsDistance) + (scoreCharge * poidsCharge) + (bonusPriorite * poidsPriorite);
    }
}
