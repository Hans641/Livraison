package com.example.livraison.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreuveLivraisonResponse {
    private Long id;
    private LocalDateTime dateHeure;
    private String nomReceptionnaire;
    private String codeConfirmation;
    private String commentaire;
}
