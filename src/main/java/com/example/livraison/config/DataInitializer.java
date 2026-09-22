package com.example.livraison.config;

import com.example.livraison.entity.Client;
import com.example.livraison.entity.Livreur;
import com.example.livraison.entity.Utilisateur;
import com.example.livraison.entity.enums.Role;
import com.example.livraison.entity.enums.StatutLivreur;
import com.example.livraison.repository.ClientRepository;
import com.example.livraison.repository.LivreurRepository;
import com.example.livraison.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Jeu de donnees minimal pour demarrer l'application sans base vide :
 * un utilisateur par role, quelques livreurs et clients de test.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final LivreurRepository livreurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() == 0) {
            creerUtilisateur("admin", "admin123", Role.ADMIN);
            creerUtilisateur("manager", "manager123", Role.MANAGER);
            creerUtilisateur("operator", "operator123", Role.OPERATOR);
            creerUtilisateur("user", "user123", Role.USER);
        }

        if (clientRepository.count() == 0) {
            clientRepository.save(Client.builder().nom("Rakoto Jean").telephone("0341234567").email("rakoto@mail.mg").adresseParDefaut("Analakely, Antananarivo").build());
            clientRepository.save(Client.builder().nom("Rasoa Marie").telephone("0339876543").email("rasoa@mail.mg").adresseParDefaut("Ivandry, Antananarivo").build());
        }

        if (livreurRepository.count() == 0) {
            livreurRepository.save(Livreur.builder().nom("Andry Tojo").telephone("0325551111").vehicule("Moto").zoneActivite("Antananarivo-Centre").statut(StatutLivreur.DISPONIBLE).capaciteMaximale(50.0).build());
            livreurRepository.save(Livreur.builder().nom("Hery Faly").telephone("0325552222").vehicule("Camionnette").zoneActivite("Antananarivo-Nord").statut(StatutLivreur.DISPONIBLE).capaciteMaximale(500.0).build());
        }
    }

    private void creerUtilisateur(String username, String password, Role role) {
        utilisateurRepository.save(Utilisateur.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .actif(true)
                .build());
    }
}
