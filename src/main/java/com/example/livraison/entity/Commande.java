package com.example.livraison.entity;

import com.example.livraison.entity.enums.PrioriteCommande;
import com.example.livraison.entity.enums.StatutCommande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "commandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private String adresseEnlevement;

    @Column(nullable = false)
    private String adresseLivraison;

    @Column(nullable = false)
    private Double poids;

    @Column(nullable = false)
    private Double volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioriteCommande priorite;

    /** distance en km, utilisee pour la tarification et l'attribution */
    @Column(nullable = false)
    private Double distance;

    /** zone de livraison, comparee a la zone d'activite du livreur */
    @Column(nullable = false)
    private String zone;

    private Double prix;

    @Column(nullable = false)
    private LocalDateTime datePrevue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCommande statut;

    @ManyToOne
    @JoinColumn(name = "livreur_id")
    private Livreur livreur;

    private LocalDateTime dateCreation;
    private LocalDateTime dateAssignation;
    private LocalDateTime datePriseEnCharge;
    private LocalDateTime dateDebutTransit;
    private LocalDateTime dateLivraisonEffective;
}
