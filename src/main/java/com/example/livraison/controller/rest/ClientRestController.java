package com.example.livraison.controller.rest;

import com.example.livraison.dto.request.ClientRequest;
import com.example.livraison.dto.response.ClientResponse;
import com.example.livraison.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> creer(@Valid @RequestBody ClientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.creer(req));
    }

    @GetMapping
    public Page<ClientResponse> lister(@RequestParam(required = false) String recherche,
                                        @PageableDefault(size = 10) Pageable pageable) {
        return clientService.lister(recherche, pageable);
    }

    @GetMapping("/{id}")
    public ClientResponse obtenir(@PathVariable Long id) {
        return clientService.obtenir(id);
    }

    @PutMapping("/{id}")
    public ClientResponse modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest req) {
        return clientService.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        clientService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
