package com.example.livraison.controller.rest;

import com.example.livraison.dto.response.IncidentResponse;
import com.example.livraison.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentRestController {

    private final IncidentService incidentService;

    @GetMapping
    public Page<IncidentResponse> lister(@PageableDefault(size = 10) Pageable pageable) {
        return incidentService.lister(pageable);
    }

    @PutMapping("/{id}/resolve")
    public IncidentResponse resoudre(@PathVariable Long id, @RequestParam String resolution) {
        return incidentService.resoudre(id, resolution);
    }
}
