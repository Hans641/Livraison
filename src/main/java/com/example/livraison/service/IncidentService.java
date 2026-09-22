package com.example.livraison.service;

import com.example.livraison.dto.request.IncidentRequest;
import com.example.livraison.dto.response.IncidentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncidentService {
    IncidentResponse signaler(Long commandeId, IncidentRequest req);
    Page<IncidentResponse> lister(Pageable pageable);
    IncidentResponse resoudre(Long incidentId, String resolution);
}
