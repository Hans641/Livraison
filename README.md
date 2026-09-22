# Livraison — Systeme de gestion d'un service de livraison

Projet Spring Boot (Projet 5 du devoir "Projets – Framework Java").

## Stack
- Java 21, Spring Boot 4.1.1
- Spring Web (MVC), Spring Data JPA, Spring Security, Thymeleaf, Validation
- MySQL

## Architecture (voir src/main/java/com/example/livraison)
config / controller (rest + web) / dto (request + response) / entity /
repository / service (+ impl) / exception / mapper / security / util

## Lancer le projet dans VSCode
1. Installer l'extension "Extension Pack for Java" + "Spring Boot Extension Pack".
2. Creer la base de donnees MySQL :
   ```sql
   CREATE DATABASE livraison_db;
   ```
   (ou laisser `createDatabaseIfNotExist=true` dans application.properties creer la base automatiquement)
3. Verifier `src/main/resources/application.properties` (identifiants MySQL : root / mot de passe vide par defaut).
4. Ouvrir le dossier dans VSCode, laisser Maven telecharger les dependances (necessite un acces internet a Maven Central).
5. Lancer avec :
   ```
   ./mvnw spring-boot:run
   ```
   ou via le bouton "Run" au-dessus de `LivraisonApplication.main()`.
6. L'application demarre sur http://localhost:8083

## Comptes de demonstration (crees automatiquement au premier lancement)
| Utilisateur | Mot de passe   | Role     |
|-------------|----------------|----------|
| admin       | admin123       | ADMIN    |
| manager     | manager123     | MANAGER  |
| operator    | operator123    | OPERATOR |
| user        | user123        | USER     |

## Interface Thymeleaf
- http://localhost:8083/login — connexion
- /dashboard — tableau de bord
- /commandes — gestion des commandes (creation, assignation, workflow, incidents)
- /livreurs — gestion des livreurs (ADMIN/MANAGER)
- /incidents — suivi et resolution des incidents
- /statistiques — statistiques filtrables (ADMIN/MANAGER)

## API REST (authentification HTTP Basic avec les memes comptes)
- POST/GET /api/orders, GET /api/orders/{id}, PUT /api/orders/{id}
- POST /api/orders/{id}/assign (assign|body {"livreurId": n} optionnel -> attribution automatique si absent)
- POST /api/orders/{id}/pickup, /start, /deliver, /cancel, /fail, /return
- POST /api/orders/{id}/incidents
- GET /api/orders/{id}/tracking, GET /api/orders/delayed
- POST/GET /api/deliverers, GET /api/deliverers/available
- POST/GET /api/clients
- GET /api/incidents, PUT /api/incidents/{id}/resolve
- GET /api/statistics/deliveries, /api/statistics/revenue

## Workflow des commandes
CREEE -> ASSIGNEE -> PRISE_EN_CHARGE -> EN_TRANSIT -> LIVREE
etats alternatifs : ANNULEE, ECHEC_LIVRAISON -> RETOURNEE

L'attribution automatique (util/ScoreCalculator) selectionne le livreur DISPONIBLE
ayant la capacite suffisante et le meilleur score (zone + charge actuelle + priorite).
La tarification (util/PrixCalculator) est configurable via application.properties.

## A completer / points d'attention pour la soutenance
- Les tests unitaires (src/test) sont a etoffer selon les exigences du cours.
- Le compte "livreur" (login pour un livreur consultant ses propres livraisons)
  n'est pas implemente : les roles geres sont ADMIN/MANAGER/OPERATOR/USER.
- Ajouter des logs/audit si demande par la grille de correction.
