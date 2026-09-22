package com.example.livraison.repository;

import com.example.livraison.entity.PreuveLivraison;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PreuveLivraisonRepository extends JpaRepository<PreuveLivraison, Long> {
    Optional<PreuveLivraison> findByCommandeId(Long commandeId);
}
