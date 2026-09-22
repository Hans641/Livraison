package com.example.livraison.service.impl;

import com.example.livraison.dto.request.LivreurRequest;
import com.example.livraison.dto.response.LivreurResponse;
import com.example.livraison.entity.Livreur;
import com.example.livraison.entity.enums.StatutCommande;
import com.example.livraison.entity.enums.StatutLivreur;
import com.example.livraison.exception.BusinessException;
import com.example.livraison.exception.ResourceNotFoundException;
import com.example.livraison.mapper.LivreurMapper;
import com.example.livraison.repository.CommandeRepository;
import com.example.livraison.repository.LivreurRepository;
import com.example.livraison.service.LivreurService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivreurServiceImpl implements LivreurService {

    private static final List<StatutCommande> STATUTS_ACTIFS = List.of(
            StatutCommande.ASSIGNEE, StatutCommande.PRISE_EN_CHARGE, StatutCommande.EN_TRANSIT);

    private final LivreurRepository livreurRepository;
    private final CommandeRepository commandeRepository;
    private final LivreurMapper livreurMapper;

    @Override
    @Transactional
    public LivreurResponse creer(LivreurRequest req) {
        Livreur livreur = livreurMapper.toEntity(req);
        return toResponse(livreurRepository.save(livreur));
    }

    @Override
    @Transactional
    public LivreurResponse modifier(Long id, LivreurRequest req) {
        Livreur livreur = trouver(id);
        if (livreur.getStatut() == StatutLivreur.EN_LIVRAISON && req.getStatut() == StatutLivreur.INDISPONIBLE) {
            throw new BusinessException("Impossible de rendre indisponible un livreur en cours de livraison");
        }
        livreurMapper.updateEntity(livreur, req);
        return toResponse(livreurRepository.save(livreur));
    }

    @Override
    @Transactional(readOnly = true)
    public LivreurResponse obtenir(Long id) {
        return toResponse(trouver(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LivreurResponse> lister(StatutLivreur statut, Pageable pageable) {
        Specification<Livreur> spec = (root, query, cb) ->
                statut == null ? cb.conjunction() : cb.equal(root.get("statut"), statut);
        return livreurRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LivreurResponse> listerDisponibles() {
        return livreurRepository.findByStatut(StatutLivreur.DISPONIBLE).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Livreur livreur = trouver(id);
        if (chargeActuelle(livreur) > 0) {
            throw new BusinessException("Impossible de supprimer un livreur ayant des livraisons en cours");
        }
        livreurRepository.delete(livreur);
    }

    private LivreurResponse toResponse(Livreur livreur) {
        return livreurMapper.toResponse(livreur, chargeActuelle(livreur));
    }

    private int chargeActuelle(Livreur livreur) {
        return (int) commandeRepository.countByLivreurIdAndStatutIn(livreur.getId(), STATUTS_ACTIFS);
    }

    private Livreur trouver(Long id) {
        return livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur introuvable : id=" + id));
    }
}
