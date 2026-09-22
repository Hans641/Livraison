package com.example.livraison.util;

import com.example.livraison.entity.enums.PrioriteCommande;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calcule le prix d'une commande en fonction de la distance, du poids, du volume
 * et de la priorite. Tarifs configurables via application.properties.
 */
@Component
public class PrixCalculator {

    @Value("${livraison.tarif.base}")
    private double tarifBase;

    @Value("${livraison.tarif.parKm}")
    private double tarifParKm;

    @Value("${livraison.tarif.parKg}")
    private double tarifParKg;

    @Value("${livraison.tarif.parM3}")
    private double tarifParM3;

    @Value("${livraison.tarif.majorationUrgente}")
    private double majorationUrgente;

    @Value("${livraison.tarif.majorationTresUrgente}")
    private double majorationTresUrgente;

    public double calculer(double distance, double poids, double volume, PrioriteCommande priorite) {
        double prix = tarifBase
                + (distance * tarifParKm)
                + (poids * tarifParKg)
                + (volume * tarifParM3);

        prix *= switch (priorite) {
            case URGENTE -> 1 + majorationUrgente;
            case TRES_URGENTE -> 1 + majorationTresUrgente;
            case NORMALE -> 1.0;
        };

        return Math.round(prix * 100.0) / 100.0;
    }
}
