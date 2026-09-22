package com.example.livraison.service;

import com.example.livraison.dto.request.ClientRequest;
import com.example.livraison.dto.response.ClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    ClientResponse creer(ClientRequest req);
    ClientResponse modifier(Long id, ClientRequest req);
    ClientResponse obtenir(Long id);
    Page<ClientResponse> lister(String recherche, Pageable pageable);
    void supprimer(Long id);
}
