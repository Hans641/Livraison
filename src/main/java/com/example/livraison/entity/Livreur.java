package com.example.livraison.entity;

import com.example.livraison.entity.enums.StatutLivreur;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "livreurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Livreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String telephone;

    @Column(nullable = false)
    private String vehicule;

    @Column(nullable = false)
    private String zoneActivite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLivreur statut;

    /** capacite en kg */
    @Column(nullable = false)
    private Double capaciteMaximale;

    @Builder.Default
    @OneToMany(mappedBy = "livreur")
    private List<Commande> commandes = new ArrayList<>();
}
