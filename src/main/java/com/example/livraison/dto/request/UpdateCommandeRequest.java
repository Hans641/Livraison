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
public class UpdateCommandeRequest {

    @NotBlank(message = "l'adresse d'enlevement est obligatoire")
    private String adresseEnlevement;

    @NotBlank(message = "l'adresse de livraison est obligatoire")
    private String adresseLivraison;

    @NotNull @Positive
    private Double poids;

    @NotNull @Positive
    private Double volume;

    @NotNull
    private PrioriteCommande priorite;

    @NotNull @Positive
    private Double distance;

    @NotBlank
    private String zone;

    @NotNull
    private LocalDateTime datePrevue;
}
