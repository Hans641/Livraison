package com.example.livraison.dto.request;

import com.example.livraison.entity.enums.StatutLivreur;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivreurRequest {

    @NotBlank(message = "le nom est obligatoire")
    private String nom;

    @NotBlank(message = "le telephone est obligatoire")
    private String telephone;

    @NotBlank(message = "le vehicule est obligatoire")
    private String vehicule;

    @NotBlank(message = "la zone d'activite est obligatoire")
    private String zoneActivite;

    @NotNull(message = "le statut est obligatoire")
    private StatutLivreur statut;

    @NotNull @Positive(message = "la capacite maximale doit etre positive")
    private Double capaciteMaximale;
}
