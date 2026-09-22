package com.example.livraison.service;

import com.example.livraison.dto.request.LivreurRequest;
import com.example.livraison.dto.response.LivreurResponse;
import com.example.livraison.entity.enums.StatutLivreur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LivreurService {
    LivreurResponse creer(LivreurRequest req);
    LivreurResponse modifier(Long id, LivreurRequest req);
    LivreurResponse obtenir(Long id);
    Page<LivreurResponse> lister(StatutLivreur statut, Pageable pageable);
    List<LivreurResponse> listerDisponibles();
    void supprimer(Long id);
}
