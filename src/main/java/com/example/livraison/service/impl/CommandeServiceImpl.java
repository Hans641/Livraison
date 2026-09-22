package com.example.livraison.service.impl;

import com.example.livraison.dto.request.CreateCommandeRequest;
import com.example.livraison.dto.request.PreuveLivraisonRequest;
import com.example.livraison.dto.request.UpdateCommandeRequest;
import com.example.livraison.dto.response.CommandeResponse;
import com.example.livraison.dto.response.PreuveLivraisonResponse;
import com.example.livraison.entity.Client;
import com.example.livraison.entity.Commande;
import com.example.livraison.entity.Livreur;
import com.example.livraison.entity.PreuveLivraison;
import com.example.livraison.entity.enums.StatutCommande;
import com.example.livraison.entity.enums.StatutLivreur;
import com.example.livraison.exception.BusinessException;
import com.example.livraison.exception.InvalidOperationException;
import com.example.livraison.exception.ResourceNotFoundException;
import com.example.livraison.mapper.CommandeMapper;
import com.example.livraison.repository.ClientRepository;
import com.example.livraison.repository.CommandeRepository;
import com.example.livraison.repository.LivreurRepository;
import com.example.livraison.repository.PreuveLivraisonRepository;
import com.example.livraison.service.CommandeService;
import com.example.livraison.util.CodeGenerator;
import com.example.livraison.util.PrixCalculator;
import com.example.livraison.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Moteur metier de gestion des livraisons.
 * Toutes les operations qui changent l'etat d'une commande et/ou d'un livreur
 * sont transactionnelles pour garantir la coherence (attribution, changement de statut).
 */
@Service
@RequiredArgsConstructor
public class CommandeServiceImpl implements CommandeService {

    private static final List<StatutCommande> STATUTS_ACTIFS_LIVREUR = List.of(
            StatutCommande.ASSIGNEE, StatutCommande.PRISE_EN_CHARGE, StatutCommande.EN_TRANSIT);

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final LivreurRepository livreurRepository;
    private final PreuveLivraisonRepository preuveLivraisonRepository;
    private final CommandeMapper commandeMapper;
    private final CodeGenerator codeGenerator;
    private final PrixCalculator prixCalculator;
    private final ScoreCalculator scoreCalculator;

    @Override
    @Transactional
    public CommandeResponse creer(CreateCommandeRequest req) {
        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : id=" + req.getClientId()));

        double prix = prixCalculator.calculer(req.getDistance(), req.getPoids(), req.getVolume(), req.getPriorite());

        Commande commande = Commande.builder()
                .numero(codeGenerator.genererNumeroCommande())
                .client(client)
                .adresseEnlevement(req.getAdresseEnlevement())
                .adresseLivraison(req.getAdresseLivraison())
                .poids(req.getPoids())
                .volume(req.getVolume())
                .priorite(req.getPriorite())
                .distance(req.getDistance())
                .zone(req.getZone())
                .prix(prix)
                .datePrevue(req.getDatePrevue())
                .statut(StatutCommande.CREEE)
                .dateCreation(LocalDateTime.now())
                .build();

        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    @Override
    @Transactional
    public CommandeResponse modifier(Long id, UpdateCommandeRequest req) {
        Commande commande = trouver(id);
        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new BusinessException("Impossible de modifier une commande deja livree");
        }
        if (commande.getStatut() != StatutCommande.CREEE) {
            throw new InvalidOperationException("La commande ne peut plus etre modifiee une fois assignee ou en cours");
        }

        commande.setAdresseEnlevement(req.getAdresseEnlevement());
        commande.setAdresseLivraison(req.getAdresseLivraison());
        commande.setPoids(req.getPoids());
        commande.setVolume(req.getVolume());
        commande.setPriorite(req.getPriorite());
        commande.setDistance(req.getDistance());
        commande.setZone(req.getZone());
        commande.setDatePrevue(req.getDatePrevue());
        commande.setPrix(prixCalculator.calculer(req.getDistance(), req.getPoids(), req.getVolume(), req.getPriorite()));

        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    @Override
    public CommandeResponse obtenir(Long id) {
        return commandeMapper.toResponse(trouver(id));
    }

    @Override
    public Page<CommandeResponse> lister(StatutCommande statut, String zone, Pageable pageable) {
        Specification<Commande> spec = (root, query, cb) -> cb.conjunction();
        if (statut != null) {
            Specification<Commande> byStatut = (root, query, cb) -> cb.equal(root.get("statut"), statut);
            spec = spec.and(byStatut);
        }
        if (zone != null && !zone.isBlank()) {
            Specification<Commande> byZone = (root, query, cb) -> cb.equal(cb.lower(root.get("zone")), zone.toLowerCase());
            spec = spec.and(byZone);
        }
        return commandeRepository.findAll(spec, pageable).map(commandeMapper::toResponse);
    }

    @Override
    public List<CommandeResponse> listerEnRetard() {
        List<StatutCommande> statutsActifs = List.of(
                StatutCommande.CREEE, StatutCommande.ASSIGNEE, StatutCommande.PRISE_EN_CHARGE, StatutCommande.EN_TRANSIT);
        return commandeRepository.findAll().stream()
                .filter(c -> statutsActifs.contains(c.getStatut()))
                .filter(c -> c.getDatePrevue().isBefore(LocalDateTime.now()))
                .map(commandeMapper::toResponse)
                .toList();
    }

    /**
     * Attribution automatique (ou manuelle si livreurIdOuNull est fourni).
     * Recherche les livreurs eligibles : disponibles, capacite suffisante,
     * puis selectionne celui avec le meilleur score si l'attribution est automatique.
     */
    @Override
    @Transactional
    public CommandeResponse assigner(Long commandeId, Long livreurIdOuNull) {
        Commande commande = trouver(commandeId);

        if (commande.getStatut() != StatutCommande.CREEE) {
            throw new InvalidOperationException("Seule une commande CREEE peut etre assignee (statut actuel : " + commande.getStatut() + ")");
        }

        Livreur livreur;
        if (livreurIdOuNull != null) {
            livreur = livreurRepository.findById(livreurIdOuNull)
                    .orElseThrow(() -> new ResourceNotFoundException("Livreur introuvable : id=" + livreurIdOuNull));
            verifierEligibilite(livreur, commande);
        } else {
            livreur = trouverMeilleurLivreur(commande)
                    .orElseThrow(() -> new BusinessException("Aucun livreur eligible disponible pour cette commande"));
        }

        commande.setLivreur(livreur);
        commande.setStatut(StatutCommande.ASSIGNEE);
        commande.setDateAssignation(LocalDateTime.now());

        if (livreur.getStatut() == StatutLivreur.DISPONIBLE) {
            livreur.setStatut(StatutLivreur.EN_LIVRAISON);
        }

        livreurRepository.save(livreur);
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    private void verifierEligibilite(Livreur livreur, Commande commande) {
        if (livreur.getStatut() != StatutLivreur.DISPONIBLE) {
            throw new BusinessException("Le livreur n'est pas disponible (statut : " + livreur.getStatut() + ")");
        }
        if (livreur.getCapaciteMaximale() < commande.getPoids()) {
            throw new BusinessException("Le livreur n'a pas la capacite suffisante pour cette commande");
        }
        List<StatutCommande> statutsActifs = List.of(StatutCommande.ASSIGNEE, StatutCommande.PRISE_EN_CHARGE, StatutCommande.EN_TRANSIT);
        List<Commande> enCours = commandeRepository.findByLivreurIdAndStatutIn(livreur.getId(), statutsActifs);
        boolean conflit = enCours.stream().anyMatch(c -> c.getDatePrevue().equals(commande.getDatePrevue()));
        if (conflit) {
            throw new BusinessException("Le livreur a deja une livraison prevue au meme horaire");
        }
    }

    private java.util.Optional<Livreur> trouverMeilleurLivreur(Commande commande) {
        return livreurRepository.findByStatut(StatutLivreur.DISPONIBLE).stream()
                .filter(l -> l.getCapaciteMaximale() >= commande.getPoids())
                .max(Comparator.comparingDouble(l -> scoreCalculator.score(l, commande, chargeActuelle(l))));
    }

    @Override
    @Transactional
    public CommandeResponse priseEnCharge(Long commandeId) {
        Commande commande = trouver(commandeId);
        if (commande.getStatut() != StatutCommande.ASSIGNEE) {
            throw new InvalidOperationException("La commande doit etre ASSIGNEE avant la prise en charge (statut actuel : " + commande.getStatut() + ")");
        }
        commande.setStatut(StatutCommande.PRISE_EN_CHARGE);
        commande.setDatePriseEnCharge(LocalDateTime.now());
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    @Override
    @Transactional
    public CommandeResponse demarrerTransit(Long commandeId) {
        Commande commande = trouver(commandeId);
        if (commande.getStatut() != StatutCommande.PRISE_EN_CHARGE) {
            throw new InvalidOperationException("La commande doit etre PRISE_EN_CHARGE avant de passer EN_TRANSIT (statut actuel : " + commande.getStatut() + ")");
        }
        commande.setStatut(StatutCommande.EN_TRANSIT);
        commande.setDateDebutTransit(LocalDateTime.now());
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    @Override
    @Transactional
    public PreuveLivraisonResponse livrer(Long commandeId, PreuveLivraisonRequest req) {
        Commande commande = trouver(commandeId);
        if (commande.getStatut() != StatutCommande.EN_TRANSIT) {
            throw new InvalidOperationException("La commande doit etre EN_TRANSIT pour etre livree (statut actuel : " + commande.getStatut() + ")");
        }

        PreuveLivraison preuve = PreuveLivraison.builder()
                .commande(commande)
                .dateHeure(LocalDateTime.now())
                .nomReceptionnaire(req.getNomReceptionnaire())
                .codeConfirmation(codeGenerator.genererCodeConfirmation())
                .commentaire(req.getCommentaire())
                .build();
        preuveLivraisonRepository.save(preuve);

        commande.setStatut(StatutCommande.LIVREE);
        commande.setDateLivraisonEffective(LocalDateTime.now());
        commandeRepository.save(commande);

        liberer(commande.getLivreur());

        return PreuveLivraisonResponse.builder()
                .id(preuve.getId())
                .dateHeure(preuve.getDateHeure())
                .nomReceptionnaire(preuve.getNomReceptionnaire())
                .codeConfirmation(preuve.getCodeConfirmation())
                .commentaire(preuve.getCommentaire())
                .build();
    }

    @Override
    @Transactional
    public CommandeResponse annuler(Long commandeId) {
        Commande commande = trouver(commandeId);
        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new BusinessException("Une commande deja livree ne peut pas etre annulee");
        }
        commande.setStatut(StatutCommande.ANNULEE);
        liberer(commande.getLivreur());
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    @Override
    @Transactional
    public CommandeResponse echecLivraison(Long commandeId, String motif) {
        Commande commande = trouver(commandeId);
        if (commande.getStatut() != StatutCommande.EN_TRANSIT) {
            throw new InvalidOperationException("Seule une commande EN_TRANSIT peut passer en ECHEC_LIVRAISON");
        }
        commande.setStatut(StatutCommande.ECHEC_LIVRAISON);
        liberer(commande.getLivreur());
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    @Override
    @Transactional
    public CommandeResponse retourner(Long commandeId) {
        Commande commande = trouver(commandeId);
        if (commande.getStatut() != StatutCommande.ECHEC_LIVRAISON) {
            throw new InvalidOperationException("Seule une commande en ECHEC_LIVRAISON peut etre RETOURNEE");
        }
        commande.setStatut(StatutCommande.RETOURNEE);
        return commandeMapper.toResponse(commandeRepository.save(commande));
    }

    private void liberer(Livreur livreur) {
        if (livreur == null) return;
        if (chargeActuelle(livreur) == 0 && livreur.getStatut() == StatutLivreur.EN_LIVRAISON) {
            livreur.setStatut(StatutLivreur.DISPONIBLE);
            livreurRepository.save(livreur);
        }
    }

    private int chargeActuelle(Livreur livreur) {
        return (int) commandeRepository.countByLivreurIdAndStatutIn(livreur.getId(), STATUTS_ACTIFS_LIVREUR);
    }

    private Commande trouver(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable : id=" + id));
    }
}
