package com.example.livraison.dto.request;

import com.example.livraison.entity.enums.PrioriteCommande;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommandeRequest {

    @NotNull(message = "le client est obligatoire")
    private Long clientId;

    @NotBlank(message = "l'adresse d'enlevement est obligatoire")
    private String adresseEnlevement;

    @NotBlank(message = "l'adresse de livraison est obligatoire")
    private String adresseLivraison;

    @NotNull @Positive(message = "le poids doit etre positif")
    private Double poids;

    @NotNull @Positive(message = "le volume doit etre positif")
    private Double volume;

    @NotNull(message = "la priorite est obligatoire")
    private PrioriteCommande priorite;

    @NotNull @Positive(message = "la distance doit etre positive")
    private Double distance;

    @NotBlank(message = "la zone est obligatoire")
    private String zone;

    @NotNull(message = "la date prevue est obligatoire")
    private LocalDateTime datePrevue;
}
