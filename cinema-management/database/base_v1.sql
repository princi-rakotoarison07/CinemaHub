-- ------------------------------
-- GESTION CINEMA - VERSION OPTIMISÉE
-- ------------------------------

-- Connexion
-- psql -U postgres
-- \c cinema_management;

-- ------------------------------
-- TYPE DE PLACE
-- ------------------------------
CREATE TABLE type_place (
  id SERIAL PRIMARY KEY,
  libelle TEXT NOT NULL UNIQUE
);

-- ------------------------------
-- CATEGORIE CLIENT
-- ------------------------------
CREATE TABLE categorie_client (
  id SERIAL PRIMARY KEY,
  libelle TEXT NOT NULL UNIQUE
);

-- ------------------------------
-- FILMS & GENRES
-- ------------------------------
CREATE TABLE film (
  id SERIAL PRIMARY KEY,
  titre TEXT NOT NULL,
  description TEXT,
  duree_minutes INT NOT NULL,
  date_sortie DATE,
  age_min INT DEFAULT 0,
  langue_originale TEXT,
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE genre (
  id SERIAL PRIMARY KEY,
  libelle TEXT UNIQUE NOT NULL
);

CREATE TABLE film_genre (
  id_film INT REFERENCES film(id) ON DELETE CASCADE,
  id_genre INT REFERENCES genre(id) ON DELETE CASCADE,
  PRIMARY KEY (id_film, id_genre)
);

-- ------------------------------
-- SALLES & PLACES
-- ------------------------------
CREATE TABLE salle (
  id SERIAL PRIMARY KEY,
  nom TEXT NOT NULL UNIQUE,
  capacite INT NOT NULL CHECK (capacite > 0),
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE place (
  id SERIAL PRIMARY KEY,
  id_salle INT NOT NULL REFERENCES salle(id) ON DELETE CASCADE,
  rangee TEXT NOT NULL,
  numero INT NOT NULL,
  id_type_place INT NOT NULL REFERENCES type_place(id),
  cree_le TIMESTAMPTZ DEFAULT now(),
  UNIQUE (id_salle, rangee, numero)
);

CREATE INDEX idx_place_salle ON place(id_salle);

-- ------------------------------
-- SEANCES
-- ------------------------------
CREATE TABLE seance (
  id SERIAL PRIMARY KEY,
  id_film INT NOT NULL REFERENCES film(id),
  id_salle INT NOT NULL REFERENCES salle(id),
  date_heure TIMESTAMPTZ NOT NULL,
  langue TEXT,
  version TEXT, -- VF, VOST, VO
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seance_film_date ON seance(id_film, date_heure);
CREATE INDEX idx_seance_salle_date ON seance(id_salle, date_heure);

-- ------------------------------
-- CLIENTS
-- ------------------------------
CREATE TABLE client (
  id SERIAL PRIMARY KEY,
  nom TEXT NOT NULL,
  prenom TEXT NOT NULL,
  email TEXT UNIQUE,
  telephone TEXT,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- STATUTS
-- ------------------------------
CREATE TABLE statut (
  id SERIAL PRIMARY KEY,
  code TEXT NOT NULL UNIQUE
);

-- ------------------------------
-- RESERVATIONS
-- ------------------------------
CREATE TABLE reservation (
  id SERIAL PRIMARY KEY,
  id_client INT NOT NULL REFERENCES client(id),
  id_seance INT NOT NULL REFERENCES seance(id),
  id_statut INT NOT NULL REFERENCES statut(id),
  montant_total NUMERIC(8,2) NOT NULL DEFAULT 0,
  date_reservation TIMESTAMPTZ DEFAULT now(),
  date_expiration TIMESTAMPTZ
);

CREATE INDEX idx_reservation_client ON reservation(id_client);
CREATE INDEX idx_reservation_seance ON reservation(id_seance);

-- ------------------------------
-- TICKETS (BILLETS)
-- ------------------------------
CREATE TABLE ticket (
  id SERIAL PRIMARY KEY,
  id_reservation INT NOT NULL REFERENCES reservation(id) ON DELETE CASCADE,
  id_place INT NOT NULL REFERENCES place(id),
  id_categorie_client INT NOT NULL REFERENCES categorie_client(id),
  prix NUMERIC(6,2) NOT NULL,
  cree_le TIMESTAMPTZ DEFAULT now(),
  UNIQUE (id_reservation, id_place)
);

CREATE INDEX idx_ticket_reservation ON ticket(id_reservation);
CREATE INDEX idx_ticket_place ON ticket(id_place);

-- ------------------------------
-- GRILLE TARIFAIRE
-- ------------------------------
CREATE TABLE tarif (
  id SERIAL PRIMARY KEY,
  id_type_place INT NOT NULL REFERENCES type_place(id),
  id_categorie_client INT NOT NULL REFERENCES categorie_client(id),
  prix NUMERIC(6,2) NOT NULL,
  actif BOOLEAN DEFAULT true,
  date_debut DATE,
  date_fin DATE,
  UNIQUE (id_type_place, id_categorie_client, date_debut)
);

-- ------------------------------
-- DONNÉES DE DÉMONSTRATION
-- ------------------------------

-- Insertion des statuts
INSERT INTO statut (code) VALUES
  ('EN_ATTENTE'),
  ('CONFIRMEE'),
  ('PAYEE'),
  ('ANNULEE');

-- Insertion des types de place
INSERT INTO type_place (libelle) VALUES
  ('STANDARD'),
  ('VIP'),
  ('PMR');

-- Insertion des catégories de client
INSERT INTO categorie_client (libelle) VALUES
  ('ADULTE'),
  ('ENFANT'),
  ('SENIOR'),
  ('ETUDIANT');

-- Insertion des genres
INSERT INTO genre (libelle) VALUES
  ('Action'),
  ('Science-Fiction'),
  ('Aventure'),
  ('Drame');

-- Insertion du film Avatar
INSERT INTO film (titre, description, duree_minutes, date_sortie, age_min, langue_originale) VALUES
  ('Avatar', 'Un marine paraplégique est envoyé sur la lune Pandora pour une mission unique', 162, '2009-12-18', 10, 'Anglais');

-- Lien film-genre
INSERT INTO film_genre (id_film, id_genre) VALUES
  (1, 1), -- Action
  (1, 2), -- Science-Fiction
  (1, 3); -- Aventure

-- Insertion des salles
INSERT INTO salle (nom, capacite) VALUES
  ('Salle 1', 150),
  ('Salle 2', 200),
  ('Salle 3', 100);

-- Insertion des places pour Salle 1
DO $$
DECLARE
  rangee_letter TEXT;
  num INT;
  type_id INT;
BEGIN
  FOREACH rangee_letter IN ARRAY ARRAY['A','B','C','D','E','F','G','H','I','J']
  LOOP
    FOR num IN 1..15 LOOP
      -- Déterminer le type de place
      IF rangee_letter IN ('A','B') AND num BETWEEN 6 AND 10 THEN
        type_id := (SELECT id FROM type_place WHERE libelle = 'VIP');
      ELSIF num = 1 OR num = 15 THEN
        type_id := (SELECT id FROM type_place WHERE libelle = 'PMR');
      ELSE
        type_id := (SELECT id FROM type_place WHERE libelle = 'STANDARD');
      END IF;
      
      INSERT INTO place (id_salle, rangee, numero, id_type_place) VALUES
        (1, rangee_letter, num, type_id);
    END LOOP;
  END LOOP;
END $$;

-- Insertion des places pour Salle 2 (exemple simplifié : 200 places standard)
DO $$
DECLARE
  rangee_letter TEXT;
  num INT;
  type_standard_id INT;
BEGIN
  type_standard_id := (SELECT id FROM type_place WHERE libelle = 'STANDARD');
  
  FOREACH rangee_letter IN ARRAY ARRAY['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P']
  LOOP
    FOR num IN 1..13 LOOP
      INSERT INTO place (id_salle, rangee, numero, id_type_place) VALUES
        (2, rangee_letter, num, type_standard_id);
    END LOOP;
  END LOOP;
END $$;

-- Insertion des places pour Salle 3 (100 places)
DO $$
DECLARE
  rangee_letter TEXT;
  num INT;
  type_standard_id INT;
BEGIN
  type_standard_id := (SELECT id FROM type_place WHERE libelle = 'STANDARD');
  
  FOREACH rangee_letter IN ARRAY ARRAY['A','B','C','D','E','F','G','H','I','J']
  LOOP
    FOR num IN 1..10 LOOP
      INSERT INTO place (id_salle, rangee, numero, id_type_place) VALUES
        (3, rangee_letter, num, type_standard_id);
    END LOOP;
  END LOOP;
END $$;

-- Insertion des séances Avatar pour le 10 janvier 2026
INSERT INTO seance (id_film, id_salle, date_heure, langue, version) VALUES
  (1, 1, '2026-01-10 10:00:00+01', 'Français', 'VF'),
  (1, 2, '2026-01-10 10:00:00+01', 'Français', 'VOST'),
  (1, 1, '2026-01-10 14:00:00+01', 'Français', 'VF'),
  (1, 1, '2026-01-10 18:00:00+01', 'Français', 'VF'),
  (1, 3, '2026-01-10 20:00:00+01', 'Français', 'VOST');

-- Insertion de la grille tarifaire
INSERT INTO tarif (id_type_place, id_categorie_client, prix, actif) VALUES
  -- STANDARD
  ((SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 9.50, true),
  ((SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'ENFANT'), 6.00, true),
  ((SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'SENIOR'), 7.50, true),
  ((SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'ETUDIANT'), 7.00, true),
  -- VIP
  ((SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 15.00, true),
  ((SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'ENFANT'), 12.00, true),
  ((SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'SENIOR'), 13.00, true),
  ((SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'ETUDIANT'), 13.00, true),
  -- PMR
  ((SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 9.50, true),
  ((SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'ENFANT'), 6.00, true),
  ((SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'SENIOR'), 7.50, true),
  ((SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'ETUDIANT'), 7.00, true);

-- Insertion d'un client exemple
INSERT INTO client (nom, prenom, email, telephone) VALUES
  ('Princi', 'Zo', 'princi@email.com', '0601020304');

-- Création d'une réservation exemple pour Avatar le 10 janvier à 10h en Salle 1
INSERT INTO reservation (id_client, id_seance, id_statut, montant_total, date_expiration) VALUES
  (1, 1, (SELECT id FROM statut WHERE code = 'EN_ATTENTE'), 19.00, now() + interval '15 minutes');

-- Ajout de tickets (2 places adultes standard)
INSERT INTO ticket (id_reservation, id_place, id_categorie_client, prix) VALUES
  (1, 
   (SELECT id FROM place WHERE id_salle = 1 AND rangee = 'E' AND numero = 7), 
   (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 
   9.50),
  (1, 
   (SELECT id FROM place WHERE id_salle = 1 AND rangee = 'E' AND numero = 8), 
   (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 
   9.50);

-- ------------------------------
-- REQUÊTES UTILES
-- ------------------------------

-- 1. Voir toutes les séances d'Avatar le 10 janvier avec disponibilité
SELECT 
  s.id,
  f.titre,
  sal.nom as salle,
  s.date_heure,
  s.version,
  sal.capacite,
  COUNT(t.id) as places_reservees,
  sal.capacite - COUNT(t.id) as places_disponibles
FROM seance s
JOIN film f ON s.id_film = f.id
JOIN salle sal ON s.id_salle = sal.id
LEFT JOIN reservation r ON r.id_seance = s.id
LEFT JOIN statut st ON r.id_statut = st.id AND st.code IN ('CONFIRMEE', 'PAYEE')
LEFT JOIN ticket t ON t.id_reservation = r.id
WHERE f.titre = 'Avatar' 
  AND DATE(s.date_heure) = '2026-01-10'
GROUP BY s.id, f.titre, sal.nom, s.date_heure, s.version, sal.capacite
ORDER BY s.date_heure;

-- 2. Voir les places disponibles pour Avatar 10 janvier 10h Salle 1 (séance id=1)
SELECT 
  p.rangee,
  p.numero,
  tp.libelle as type_place,
  CASE WHEN t.id IS NULL THEN 'DISPONIBLE' ELSE 'OCCUPEE' END as statut,
  tar.prix as prix_adulte
FROM place p
JOIN type_place tp ON p.id_type_place = tp.id
LEFT JOIN ticket t ON t.id_place = p.id 
  AND t.id_reservation IN (
    SELECT id FROM reservation 
    WHERE id_seance = 1 
    AND id_statut IN (
      SELECT id FROM statut WHERE code IN ('CONFIRMEE', 'PAYEE', 'EN_ATTENTE')
    )
  )
LEFT JOIN tarif tar ON tar.id_type_place = p.id_type_place 
  AND tar.id_categorie_client = (SELECT id FROM categorie_client WHERE libelle = 'ADULTE')
  AND tar.actif = true
WHERE p.id_salle = (SELECT id_salle FROM seance WHERE id = 1)
ORDER BY p.rangee, p.numero;

-- 3. Voir les réservations d'un client
SELECT 
  r.id,
  f.titre,
  s.date_heure,
  sal.nom as salle,
  s.version,
  st.code as statut,
  r.montant_total,
  COUNT(t.id) as nombre_places,
  STRING_AGG(p.rangee || p.numero, ', ' ORDER BY p.rangee, p.numero) as places
FROM reservation r
JOIN statut st ON r.id_statut = st.id
JOIN seance s ON r.id_seance = s.id
JOIN film f ON s.id_film = f.id
JOIN salle sal ON s.id_salle = sal.id
LEFT JOIN ticket t ON t.id_reservation = r.id
LEFT JOIN place p ON t.id_place = p.id
WHERE r.id_client = 1
GROUP BY r.id, f.titre, s.date_heure, sal.nom, s.version, st.code, r.montant_total
ORDER BY s.date_heure DESC;

-- 4. Calculer le prix d'une réservation selon type place et catégorie client
SELECT 
  tp.libelle as type_place,
  cc.libelle as categorie_client,
  t.prix
FROM tarif t
JOIN type_place tp ON t.id_type_place = tp.id
JOIN categorie_client cc ON t.id_categorie_client = cc.id
WHERE t.actif = true
ORDER BY tp.libelle, cc.libelle;