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
  nb_place INT NOT NULL DEFAULT 0 CHECK (nb_place >= 0),
  montant_total NUMERIC(12,2) NOT NULL DEFAULT 0,
  date_reservation TIMESTAMPTZ DEFAULT now(),
  date_expiration TIMESTAMPTZ
);

CREATE INDEX idx_reservation_client ON reservation(id_client);
CREATE INDEX idx_reservation_seance ON reservation(id_seance);

CREATE TABLE details_reservation (
  id SERIAL PRIMARY KEY,
  id_reservation INT NOT NULL REFERENCES reservation(id) ON DELETE CASCADE,
  id_place INT NOT NULL REFERENCES place(id),
  id_categorie_client INT NOT NULL REFERENCES categorie_client(id),
  is_actif BOOLEAN DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now(),
  UNIQUE (id_reservation, id_place)
);

CREATE INDEX idx_details_reservation_reservation ON details_reservation(id_reservation);
CREATE INDEX idx_details_reservation_place ON details_reservation(id_place);

-- ------------------------------
-- TICKETS (BILLETS)
-- ------------------------------
CREATE TABLE ticket (
  id SERIAL PRIMARY KEY,
  id_reservation INT NOT NULL REFERENCES reservation(id) ON DELETE CASCADE,
  id_place INT NOT NULL REFERENCES place(id),
  id_categorie_client INT NOT NULL REFERENCES categorie_client(id),
  prix NUMERIC(12,2) NOT NULL,
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
  prix NUMERIC(12,2) NOT NULL,
  actif BOOLEAN DEFAULT true,
  date_debut DATE,
  date_fin DATE,
  UNIQUE (id_type_place, id_categorie_client, date_debut)
);
--priorite tarif 
CREATE TABLE configuration_tarif (
  id SERIAL PRIMARY KEY,
  id_tarif1 INT NOT NULL REFERENCES tarif(id) ON DELETE CASCADE,
  id_tarif2 INT NOT NULL REFERENCES tarif(id) ON DELETE CASCADE,
  pourcentage NUMERIC(5,2) NOT NULL CHECK (pourcentage >= 0 AND pourcentage <= 100),
  actif BOOLEAN DEFAULT true,
  UNIQUE (id_tarif2)
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

-- Insertion du film Titanic
INSERT INTO film (titre, description, duree_minutes, date_sortie, age_min, langue_originale) VALUES
  ('Titanic', 'Un marine paraplégique est envoyé sur la lune Pandora pour une mission unique', 60, '2009-12-18', 10, 'Anglais');

-- Lien film-genre
INSERT INTO film_genre (id_film, id_genre) VALUES
  (1, 1), -- Action
  (1, 2), -- Science-Fiction
  (1, 3); -- Aventure

-- Insertion des salles
INSERT INTO salle (nom, capacite) VALUES
  ('Salle 1', 10),
  ('Salle 2', 100);


-- Insertion des places pour Salle 1 (10 places : 6 standard, 4 PMR)
DO $$
DECLARE
  num INT;
  type_standard_id INT;
  type_pmr_id INT;
  type_id INT;
BEGIN
  type_standard_id := (SELECT id FROM type_place WHERE libelle = 'STANDARD');
  type_pmr_id := (SELECT id FROM type_place WHERE libelle = 'PMR');

  FOR num IN 1..10 LOOP
    IF num BETWEEN 1 AND 4 THEN
      type_id := type_pmr_id;
    ELSE
      type_id := type_standard_id;
    END IF;

    INSERT INTO place (id_salle, rangee, numero, id_type_place) VALUES
      (1, 'A', num, type_id);
  END LOOP;
END $$;


DO $$
DECLARE
  num INT;
  type_standard_id INT;
  type_pmr_id INT;
  type_vip_id INT;
  type_id INT;
BEGIN
  type_standard_id := (SELECT id FROM type_place WHERE libelle = 'STANDARD');
  type_pmr_id := (SELECT id FROM type_place WHERE libelle = 'PMR');
  type_vip_id := (SELECT id FROM type_place WHERE libelle = 'VIP');

  FOR num IN 1..100 LOOP
    IF num BETWEEN 1 AND 20 THEN
      type_id := type_vip_id;
    ELSIF num BETWEEN 21 AND 50 THEN
      type_id := type_pmr_id;
    ELSE
      type_id := type_standard_id;
    END IF;

    INSERT INTO place (id_salle, rangee, numero, id_type_place) VALUES
      (2, 'A', num, type_id);
  END LOOP;
END $$;

-- Insertion des séances Avatar pour le 10 janvier 2026
INSERT INTO seance (id_film, id_salle, date_heure, langue, version) VALUES
  (1, 2, '2026-01-20 10:00:00+01', 'Français', 'VF'),
  (1, 2, '2026-01-21 10:00:00+01', 'Français', 'VOST'),
  (1, 2, '2026-01-21 15:00:00+01', 'Français', 'VOST');



-- Insertion de la grille tarifaire
INSERT INTO tarif (id, id_type_place, id_categorie_client, prix, actif) VALUES
  (3, (SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'SENIOR'), 20000, true),
  (4, (SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'ETUDIANT'), 20000, true),
  (1, (SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 30000, true),
  (5, (SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 50000, true),
  (6, (SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'ENFANT'), 10000, true),
  (7, (SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'SENIOR'), 100000, true),
  (8, (SELECT id FROM type_place WHERE libelle = 'VIP'), (SELECT id FROM categorie_client WHERE libelle = 'ETUDIANT'), 45000, true),
  (9, (SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'ADULTE'), 40000, true),
  (10, (SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'ENFANT'), 30000, true),
  (11, (SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'SENIOR'), 50000, true),
  (12, (SELECT id FROM type_place WHERE libelle = 'PMR'), (SELECT id FROM categorie_client WHERE libelle = 'ETUDIANT'), 30000, true),
  (2, (SELECT id FROM type_place WHERE libelle = 'STANDARD'), (SELECT id FROM categorie_client WHERE libelle = 'ENFANT'), 15000, true);

SELECT setval('tarif_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tarif));

INSERT INTO configuration_tarif (id, id_tarif1, id_tarif2, pourcentage, actif) VALUES
  (1, 1, 2, 50, true),
  (2, 5, 6, 50, true),
  (3, 9, 10, 50, true);

SELECT setval('configuration_tarif_id_seq', (SELECT COALESCE(MAX(id), 1) FROM configuration_tarif));

-- Insertion d'un client exemple
INSERT INTO client (nom, prenom, email, telephone) VALUES
  ('Princi', 'Rkt', 'princi@email.com', '0601020304'),
  ('Funaki', 'Lvi', 'funaki@email.com', '0601020304');

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