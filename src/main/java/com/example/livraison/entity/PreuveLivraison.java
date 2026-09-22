package com.example.livraison.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "preuves_livraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreuveLivraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "commande_id", nullable = false, unique = true)
    private Commande commande;

    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private String nomReceptionnaire;

    @Column(nullable = false)
    private String codeConfirmation;

    private String commentaire;
}
