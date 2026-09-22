package com.example.livraison.repository;

import com.example.livraison.entity.Commande;
import com.example.livraison.entity.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommandeRepository extends JpaRepository<Commande, Long>, JpaSpecificationExecutor<Commande> {

    Optional<Commande> findByNumero(String numero);

    List<Commande> findByStatutAndDatePrevueBefore(StatutCommande statut, LocalDateTime dateLimite);

    List<Commande> findByLivreurIdAndStatutIn(Long livreurId, List<StatutCommande> statuts);

    long countByLivreurIdAndStatutIn(Long livreurId, List<StatutCommande> statuts);

    long countByStatut(StatutCommande statut);
}
