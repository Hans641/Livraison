package com.example.livraison.mapper;

import com.example.livraison.dto.request.ClientRequest;
import com.example.livraison.dto.response.ClientResponse;
import com.example.livraison.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientRequest req) {
        return Client.builder()
                .nom(req.getNom())
                .telephone(req.getTelephone())
                .email(req.getEmail())
                .adresseParDefaut(req.getAdresseParDefaut())
                .build();
    }

    public void updateEntity(Client client, ClientRequest req) {
        client.setNom(req.getNom());
        client.setTelephone(req.getTelephone());
        client.setEmail(req.getEmail());
        client.setAdresseParDefaut(req.getAdresseParDefaut());
    }

    public ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .telephone(client.getTelephone())
                .email(client.getEmail())
                .adresseParDefaut(client.getAdresseParDefaut())
                .build();
    }
}
