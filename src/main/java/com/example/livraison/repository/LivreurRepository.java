package com.example.livraison.repository;

import com.example.livraison.entity.Livreur;
import com.example.livraison.entity.enums.StatutLivreur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LivreurRepository extends JpaRepository<Livreur, Long>, JpaSpecificationExecutor<Livreur> {
    List<Livreur> findByStatut(StatutLivreur statut);
    List<Livreur> findByZoneActiviteAndStatut(String zoneActivite, StatutLivreur statut);
}
