package com.example.livraison.dto.request;

import com.example.livraison.entity.enums.TypeIncident;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {

    @NotNull(message = "le type d'incident est obligatoire")
    private TypeIncident type;

    private String description;

    private String resolution;
}
