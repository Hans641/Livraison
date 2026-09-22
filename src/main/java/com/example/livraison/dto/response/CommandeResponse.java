package com.example.livraison.dto.response;

import com.example.livraison.entity.enums.PrioriteCommande;
import com.example.livraison.entity.enums.StatutCommande;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeResponse {
    private Long id;
    private String numero;
    private Long clientId;
    private String nomClient;
    private String adresseEnlevement;
    private String adresseLivraison;
    private Double poids;
    private Double volume;
    private PrioriteCommande priorite;
    private Double distance;
    private String zone;
    private Double prix;
    private LocalDateTime datePrevue;
    private StatutCommande statut;
    private Long livreurId;
    private String nomLivreur;
    private boolean enRetard;
}
