package com.example.livraison.service.impl;

import com.example.livraison.dto.request.IncidentRequest;
import com.example.livraison.dto.response.IncidentResponse;
import com.example.livraison.entity.Commande;
import com.example.livraison.entity.Incident;
import com.example.livraison.exception.ResourceNotFoundException;
import com.example.livraison.mapper.IncidentMapper;
import com.example.livraison.repository.CommandeRepository;
import com.example.livraison.repository.IncidentRepository;
import com.example.livraison.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final CommandeRepository commandeRepository;
    private final IncidentMapper incidentMapper;

    @Override
    @Transactional
    public IncidentResponse signaler(Long commandeId, IncidentRequest req) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : id=" + commandeId));

        Incident incident = Incident.builder()
                .type(req.getType())
                .date(LocalDateTime.now())
                .description(req.getDescription())
                .commande(commande)
                .livreur(commande.getLivreur())
                .resolution(req.getResolution())
                .build();

        return incidentMapper.toResponse(incidentRepository.save(incident));
    }

    @Override
    public Page<IncidentResponse> lister(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(incidentMapper::toResponse);
    }

    @Override
    @Transactional
    public IncidentResponse resoudre(Long incidentId, String resolution) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident introuvable : id=" + incidentId));
        incident.setResolution(resolution);
        return incidentMapper.toResponse(incidentRepository.save(incident));
    }
}
