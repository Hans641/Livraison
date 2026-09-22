package com.example.livraison.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {

    @NotBlank(message = "le nom est obligatoire")
    private String nom;

    @NotBlank(message = "le telephone est obligatoire")
    private String telephone;

    @Email(message = "email invalide")
    private String email;

    private String adresseParDefaut;
}
