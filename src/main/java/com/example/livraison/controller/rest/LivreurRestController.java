package com.example.livraison.controller.rest;

import com.example.livraison.dto.request.LivreurRequest;
import com.example.livraison.dto.response.LivreurResponse;
import com.example.livraison.entity.enums.StatutLivreur;
import com.example.livraison.service.LivreurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LivreurRestController {

    private final LivreurService livreurService;

    @PostMapping("/api/deliverers")
    public ResponseEntity<LivreurResponse> creer(@Valid @RequestBody LivreurRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livreurService.creer(req));
    }

    @GetMapping("/api/deliverers")
    public Page<LivreurResponse> lister(@RequestParam(required = false) StatutLivreur statut,
                                         @PageableDefault(size = 10) Pageable pageable) {
        return livreurService.lister(statut, pageable);
    }

    @GetMapping("/api/deliverers/available")
    public List<LivreurResponse> disponibles() {
        return livreurService.listerDisponibles();
    }

    @GetMapping("/api/deliverers/{id}")
    public LivreurResponse obtenir(@PathVariable Long id) {
        return livreurService.obtenir(id);
    }

    @PutMapping("/api/deliverers/{id}")
    public LivreurResponse modifier(@PathVariable Long id, @Valid @RequestBody LivreurRequest req) {
        return livreurService.modifier(id, req);
    }

    @DeleteMapping("/api/deliverers/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        livreurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
