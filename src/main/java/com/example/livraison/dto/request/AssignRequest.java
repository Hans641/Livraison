package com.example.livraison.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * livreurId optionnel : si absent, le systeme choisit automatiquement
 * le meilleur livreur eligible via le score d'attribution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRequest {
    private Long livreurId;
}
