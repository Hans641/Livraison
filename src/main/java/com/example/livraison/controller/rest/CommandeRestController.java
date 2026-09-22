package com.example.livraison.controller.rest;

import com.example.livraison.dto.request.AssignRequest;
import com.example.livraison.dto.request.CreateCommandeRequest;
import com.example.livraison.dto.request.IncidentRequest;
import com.example.livraison.dto.request.PreuveLivraisonRequest;
import com.example.livraison.dto.request.UpdateCommandeRequest;
import com.example.livraison.dto.response.CommandeResponse;
import com.example.livraison.dto.response.IncidentResponse;
import com.example.livraison.dto.response.PreuveLivraisonResponse;
import com.example.livraison.entity.enums.StatutCommande;
import com.example.livraison.service.CommandeService;
import com.example.livraison.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST pour la gestion des commandes de livraison.
 * Suit exactement les endpoints attendus par le cahier des charges (Projet 5).
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CommandeRestController {

    private final CommandeService commandeService;
    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<CommandeResponse> creer(@Valid @RequestBody CreateCommandeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandeService.creer(req));
    }

    @GetMapping
    public Page<CommandeResponse> lister(
            @RequestParam(required = false) StatutCommande status,
            @RequestParam(required = false) String zone,
            @PageableDefault(size = 10) Pageable pageable) {
        return commandeService.lister(status, zone, pageable);
    }

    @GetMapping("/{id}")
    public CommandeResponse obtenir(@PathVariable Long id) {
        return commandeService.obtenir(id);
    }

    @PutMapping("/{id}")
    public CommandeResponse modifier(@PathVariable Long id, @Valid @RequestBody UpdateCommandeRequest req) {
        return commandeService.modifier(id, req);
    }

    @PostMapping("/{id}/assign")
    public CommandeResponse assigner(@PathVariable Long id, @RequestBody(required = false) AssignRequest req) {
        Long livreurId = req != null ? req.getLivreurId() : null;
        return commandeService.assigner(id, livreurId);
    }

    @PostMapping("/{id}/pickup")
    public CommandeResponse priseEnCharge(@PathVariable Long id) {
        return commandeService.priseEnCharge(id);
    }

    @PostMapping("/{id}/start")
    public CommandeResponse demarrer(@PathVariable Long id) {
        return commandeService.demarrerTransit(id);
    }

    @PostMapping("/{id}/deliver")
    public PreuveLivraisonResponse livrer(@PathVariable Long id, @Valid @RequestBody PreuveLivraisonRequest req) {
        return commandeService.livrer(id, req);
    }

    @PostMapping("/{id}/cancel")
    public CommandeResponse annuler(@PathVariable Long id) {
        return commandeService.annuler(id);
    }

    @PostMapping("/{id}/incidents")
    public ResponseEntity<IncidentResponse> signalerIncident(@PathVariable Long id, @Valid @RequestBody IncidentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.signaler(id, req));
    }

    @GetMapping("/{id}/tracking")
    public CommandeResponse suivi(@PathVariable Long id) {
        return commandeService.obtenir(id);
    }

    @GetMapping("/delayed")
    public List<CommandeResponse> enRetard() {
        return commandeService.listerEnRetard();
    }

    // Endpoints complementaires au workflow (echec de livraison / retour)
    @PostMapping("/{id}/fail")
    public CommandeResponse echec(@PathVariable Long id, @RequestParam(required = false) String motif) {
        return commandeService.echecLivraison(id, motif);
    }

    @PostMapping("/{id}/return")
    public CommandeResponse retourner(@PathVariable Long id) {
        return commandeService.retourner(id);
    }
}
