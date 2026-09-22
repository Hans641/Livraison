package com.example.livraison.service;

import com.example.livraison.dto.request.CreateCommandeRequest;
import com.example.livraison.dto.request.PreuveLivraisonRequest;
import com.example.livraison.dto.request.UpdateCommandeRequest;
import com.example.livraison.dto.response.CommandeResponse;
import com.example.livraison.dto.response.PreuveLivraisonResponse;
import com.example.livraison.entity.enums.StatutCommande;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommandeService {

    CommandeResponse creer(CreateCommandeRequest req);

    CommandeResponse modifier(Long id, UpdateCommandeRequest req);

    CommandeResponse obtenir(Long id);

    Page<CommandeResponse> lister(StatutCommande statut, String zone, Pageable pageable);

    List<CommandeResponse> listerEnRetard();

    CommandeResponse assigner(Long commandeId, Long livreurIdOuNull);

    CommandeResponse priseEnCharge(Long commandeId);

    CommandeResponse demarrerTransit(Long commandeId);

    PreuveLivraisonResponse livrer(Long commandeId, PreuveLivraisonRequest req);

    CommandeResponse annuler(Long commandeId);

    CommandeResponse echecLivraison(Long commandeId, String motif);

    CommandeResponse retourner(Long commandeId);
}
