
# CinemaHub — Documentation projet

## 1. Présentation

CinemaHub est une application de gestion de cinéma composée de :

- **Backend** : `cinema-management` (Spring Boot, Java 17) + PostgreSQL
- **Frontend** : `cinema-front` (Vue 3 + Vite, template NiceAdmin)

Le projet couvre :

- **Gestion cinéma** : films, salles, places, séances
- **Réservation & billetterie** : sélection de places, réservation, paiement, tickets
- **Tarification** : tarifs par type de place & catégorie client + configuration (pourcentage)
- **Publicité** : sociétés, vidéos publicitaires, tarifs pub, contrats, diffusions, paiements, facturation
- **Chiffres d’affaires** : par séance (tickets + publicité)

## 2. Règles de gestion (extrait)

### 2.1 Salles / Séances / Films

- Il y a plusieurs salles de cinéma.
- Chaque salle a une capacité (dérivée du nombre de places créées pour la salle).
- Un film peut être diffusé en parallèle dans plusieurs salles.
- Un film peut être diffusé plusieurs fois dans une journée.
- Un film peut être diffusé sur plusieurs jours.

### 2.2 Réservation

- Un client réserve **une ou plusieurs places** pour une **séance**.
- Une place est considérée comme **occupée** pour une séance si elle est présente dans des `DetailsReservation` actifs (`isActif=true`) d’une réservation dont le statut est dans :
  - `EN_ATTENTE`, `CONFIRMEE`, `PAYEE`
- La réservation est créée avec une **expiration** (par défaut **15 minutes**).
- Le montant total est calculé au moment du **paiement** (création des tickets).

### 2.3 Types de places & tarifs

- Les places sont associées à un **type de place** (ex: premium, standard).
- Le prix payé dépend :
  - du **type de place**
  - de la **catégorie client**
  - du **tarif actif** le plus récent (`Tarif.actif=true` + `dateDebut`)
- Une configuration optionnelle (`ConfigurationTarif`) peut appliquer un **pourcentage** en se basant sur un tarif de référence.

### 2.4 Publicité

- Des sociétés peuvent diffuser des vidéos publicitaires avant le début de diffusion des films.
- Le coût est un **prix par diffusion** (paramétrable via `TarifPublicite`).
- Un **contrat publicitaire** porte sur :
  - une vidéo publicitaire
  - un tarif publicitaire
  - un nombre total de diffusions
  - une période (date début / fin)
- Une séance peut contenir **plusieurs diffusions** d’une même société (via `nombrePub`).
- La création d’un contrat « with diffusions » impose la contrainte :
  - **la somme des `nombrePub` par séance = `nbDiffusions`** du contrat.

## 3. Architecture & structure du dépôt

### 3.1 Monorepo

- `cinema-management/` : API REST, logique métier, accès DB
- `cinema-front/` : interface d’administration (dashboard)

### 3.2 Base URL

Le backend utilise un `context-path` :

- `http://localhost:8080/cinema-management`

Les endpoints REST sont sous :

- `http://localhost:8080/cinema-management/api/...`

Le front en dev est servi sous :

- `http://localhost:5173/cinema-front/`

## 4. Installation & exécution

### 4.1 Prérequis

- Java 17
- Maven
- Node.js (LTS recommandé)
- PostgreSQL

### 4.2 Base de données

Créer une base PostgreSQL `cinema_management`.

Configuration attendue (par défaut) :

- DB : `cinema_management`
- user : `postgres`
- pass : `postgres`

Le backend est configuré dans :

- `cinema-management/src/main/resources/application.properties`

Des scripts SQL sont disponibles dans :

- `cinema-management/database/*.sql`

### 4.3 Lancer le backend

Dans `cinema-management` :

```bash
mvn spring-boot:run
```

### 4.4 Lancer le frontend

Dans `cinema-front` :

```bash
npm install
npm run dev
```

Configurer l’URL backend côté front via :

- `cinema-front/.env`

Exemple :

```
VITE_API_BASE_URL=http://localhost:8080/cinema-management
```

Après modification du `.env`, redémarrer Vite.

## 5. Modèle fonctionnel (domain)

### 5.1 Cinéma

- **Salle** : salle de projection
- **Place** : siège (rangée + numéro) attaché à une salle
- **TypePlace** : typologie du siège (premium, standard, etc.)
- **Film** : film projeté
- **Séance** : projection d’un film dans une salle à une date/heure

### 5.2 Réservation & billetterie

- **Client** : personne qui réserve
- **CategorieClient** : catégorie (ex: adulte, étudiant, etc.)
- **Reservation** : entête de réservation (client, séance, statut, nb places, total)
- **DetailsReservation** : lignes de réservation (place + catégorie client), avec `isActif` pour activer/désactiver une ligne
- **Ticket** : généré lors du paiement, contient prix final par place

#### Statuts de réservation (utilisés)

- `EN_ATTENTE`
- `CONFIRMEE`
- `PAYEE`
- `ANNULEE`

### 5.3 Tarification

- **Tarif** : prix par (TypePlace, CategorieClient) avec période et `actif`
- **ConfigurationTarif** : règle optionnelle de calcul via pourcentage

### 5.4 Publicité

- **Societe** : annonceur
- **VideoPublicitaire** : média publicitaire (titre, durée, société)
- **TarifPublicite** : prix par diffusion (période, actif)
- **ContratPublicite** : contrat liant vidéo + tarif + nb diffusions + montant total
- **DiffusionPublicite** : association contrat ↔ séance + `nombrePub`
- **PaiementContratPublicite** : paiement partiel/total d’un contrat

## 6. Parcours clés

### 6.1 Réserver des places (workflow)

1. **Créer/choisir** un `Client`.
2. **Créer/choisir** une `Séance`.
3. Récupérer les places avec l’occupation : `GET /api/seances/{id}/places`.
4. Créer une réservation : `POST /api/reservations` avec `clientId`, `seanceId` et `items` (place + catégorie).
5. Optionnel : modifier la sélection : `PUT /api/reservations/{id}`.
6. Prévisualiser le total : `GET /api/reservations/{id}/pay-preview`.
7. Payer : `PUT /api/reservations/{id}/pay`.
8. Consulter les tickets : `GET /api/tickets?reservationId=...`.

### 6.2 Publicité (workflow)

1. Créer une `Societe`.
2. Créer une `VideoPublicitaire` rattachée à la société.
3. Définir un `TarifPublicite` actif (prix par diffusion).
4. Créer un `ContratPublicite` :
   - soit via `POST /api/contrats-publicite` (contrat simple)
   - soit via `POST /api/contrats-publicite/with-diffusions` (contrat + distribution des diffusions par séance)
5. Consulter les diffusions du contrat : `GET /api/diffusions-publicite?contratId=...`.
6. Enregistrer un paiement : `POST /api/paiements-contrats-publicite`.
7. Suivre la facturation : `GET /api/contrats-publicite/factures` et `GET /api/contrats-publicite/{id}/facture-details`.

## 7. API REST (backend)

Tous les endpoints ci-dessous sont préfixés par :

- `/cinema-management` (context-path)
- puis `/api/...`

### 7.1 Films

- `GET /api/films`
- `GET /api/films/{id}`
- `POST /api/films`
- `PUT /api/films/{id}`
- `DELETE /api/films/{id}`

### 7.2 Salles

- `GET /api/salles`
- `GET /api/salles/{id}`
- `POST /api/salles`
- `PUT /api/salles/{id}`
- `DELETE /api/salles/{id}`

### 7.3 Places

- `GET /api/places` (filtre : `?salleId=...`)
- `GET /api/places/{id}`
- `POST /api/places`
- `PUT /api/places/{id}`
- `DELETE /api/places/{id}`

### 7.4 Séances

- `GET /api/seances`
- `GET /api/seances/{id}`
- `POST /api/seances`
- `PUT /api/seances/{id}`
- `DELETE /api/seances/{id}`
- `GET /api/seances/by-interval?dateDebut=YYYY-MM-DD&dateFin=YYYY-MM-DD`
- `GET /api/seances/{id}/places` (retourne `occupee` par place)
- `GET /api/seances/chiffres-affaire?dateDebut=YYYY-MM-DD&dateFin=YYYY-MM-DD`
- `GET /api/seances/{id}/chiffres-affaire-details`

### 7.5 Clients

- `GET /api/clients`
- `GET /api/clients/{id}`
- `POST /api/clients`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

### 7.6 Catégories client

- `GET /api/categories-clients`
- `GET /api/categories-clients/{id}`

### 7.7 Réservations

- `GET /api/reservations`
- `GET /api/reservations/{id}` (inclut les détails actifs)
- `POST /api/reservations`
- `PUT /api/reservations/{id}`
- `PUT /api/reservations/{id}/pay`
- `GET /api/reservations/{id}/pay-preview`
- `DELETE /api/reservations/{id}`

### 7.8 Détails de réservation

- `GET /api/details-reservations` (filtre : `?reservationId=...`)
- `GET /api/details-reservations/{id}`

### 7.9 Tickets

- `GET /api/tickets` (filtre : `?reservationId=...`)
- `GET /api/tickets/{id}`
- `POST /api/tickets`
- `PUT /api/tickets/{id}`
- `DELETE /api/tickets/{id}`

### 7.10 Tarifs (billetterie)

- `GET /api/tarifs`
- `GET /api/tarifs/{id}`
- `POST /api/tarifs`
- `PUT /api/tarifs/{id}`
- `DELETE /api/tarifs/{id}`

### 7.11 Configuration tarifs

- `GET /api/configuration-tarifs`
- `GET /api/configuration-tarifs/{id}`
- `POST /api/configuration-tarifs`
- `PUT /api/configuration-tarifs/{id}`
- `DELETE /api/configuration-tarifs/{id}`

### 7.12 Publicité — Sociétés

- `GET /api/societes`
- `GET /api/societes/{id}`
- `POST /api/societes`
- `PUT /api/societes/{id}`
- `DELETE /api/societes/{id}`

### 7.13 Publicité — Vidéos publicitaires

- `GET /api/videos-publicitaires`
- `GET /api/videos-publicitaires/{id}`
- `POST /api/videos-publicitaires`
- `PUT /api/videos-publicitaires/{id}`
- `DELETE /api/videos-publicitaires/{id}`

### 7.14 Publicité — Tarifs

- `GET /api/tarifs-publicite`
- `GET /api/tarifs-publicite/{id}`
- `POST /api/tarifs-publicite`
- `PUT /api/tarifs-publicite/{id}`
- `DELETE /api/tarifs-publicite/{id}`

### 7.15 Publicité — Contrats & facturation

- `GET /api/contrats-publicite`
- `GET /api/contrats-publicite/{id}`
- `POST /api/contrats-publicite`
- `PUT /api/contrats-publicite/{id}`
- `DELETE /api/contrats-publicite/{id}`
- `POST /api/contrats-publicite/with-diffusions`
- `GET /api/contrats-publicite/factures`
- `GET /api/contrats-publicite/{id}/facture-details`

### 7.16 Publicité — Diffusions

- `GET /api/diffusions-publicite?contratId=...`

### 7.17 Publicité — Paiements contrats

- `GET /api/paiements-contrats-publicite` (filtres : `?contratId=...` ou `?societeId=...`)
- `GET /api/paiements-contrats-publicite/{id}`
- `POST /api/paiements-contrats-publicite`
- `DELETE /api/paiements-contrats-publicite/{id}`

## 8. Frontend (routes principales)

Base path : `/cinema-front/`.

Routes principales :

- Dashboard : `/cinema-front/`
- Films : `/cinema-front/films`
- Salles : `/cinema-front/salles`
- Séances : `/cinema-front/seances`
- Chiffres d’affaires séance : `/cinema-front/seances/chiffres-affaire`
- Places : `/cinema-front/places` et `/cinema-front/places/gestion`
- Tarifs : `/cinema-front/tarifs` + `/cinema-front/tarifs/grille`
- Configuration tarifs : `/cinema-front/configuration-tarifs/grille`
- Clients : `/cinema-front/clients`
- Réservations : `/cinema-front/reservations` + `/cinema-front/reservations/:id/edit`
- Publicités :
  - Contrats : `/cinema-front/publicites/contrats`
  - Payer contrat : `/cinema-front/publicites/contrats/:id/payer`
  - Chiffre d’affaires pub : `/cinema-front/publicites/chiffres-affaire`
  - Config sociétés : `/cinema-front/publicites/config/societes`
  - Config vidéos : `/cinema-front/publicites/config/videos-publicitaires`
  - Config tarifs pub : `/cinema-front/publicites/config/tarifs-publicite`

## 9. Notes techniques importantes

### 9.1 Occupation des places

La disponibilité des places est basée sur `DetailsReservation` (actifs) + statut réservation.

Endpoint clé (pour l’affichage front) :

- `GET /api/seances/{id}/places`

### 9.2 Calcul du chiffre d’affaires

Le service `SeanceChiffreAffaireService` calcule :

- **CA tickets** : somme des prix appliqués par place (tarif + configuration)
- **CA publicité** : somme des diffusions * prix par diffusion
- **CA total** : tickets + publicité

