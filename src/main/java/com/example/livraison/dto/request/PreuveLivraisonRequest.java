package com.example.livraison.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreuveLivraisonRequest {

    @NotBlank(message = "le nom du receptionnaire est obligatoire")
    private String nomReceptionnaire;

    private String commentaire;
}
