psql -U postgres 

\c cinema_management;
-- ------------------------------
-- GESTION CINEMA - VERSION FINALE
-- ------------------------------

-- ------------------------------
-- TYPE DE PLACE
-- ------------------------------
CREATE TABLE type_place (
  id SERIAL PRIMARY KEY,
  libelle TEXT NOT NULL -- STANDARD, VIP, PMR
);

-- ------------------------------
-- CATEGORIE PERSONNE
-- ------------------------------
CREATE TABLE categorie_personne (
  id SERIAL PRIMARY KEY,
  libelle TEXT NOT NULL -- ADULTE, ENFANT, SENIOR...
);

-- ------------------------------
-- FILMS & GENRES
-- ------------------------------
CREATE TABLE film (
  id SERIAL PRIMARY KEY,
  titre TEXT NOT NULL,
  description TEXT,
  duree_minutes INT,
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
  nom TEXT NOT NULL,
  capacite INT NOT NULL CHECK (capacite > 0),
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE place (
  id SERIAL PRIMARY KEY,
  id_salle INT REFERENCES salle(id) ON DELETE CASCADE,
  rangee TEXT,
  numero INT,
  code_place TEXT,
  id_type_place INT REFERENCES type_place(id),
  cree_le TIMESTAMPTZ DEFAULT now(),
  UNIQUE (id_salle, rangee, numero)
);

-- ------------------------------
-- SEANCES
-- ------------------------------
CREATE TABLE seance (
  id SERIAL PRIMARY KEY,
  id_film INT REFERENCES film(id),
  id_salle INT REFERENCES salle(id),
  debut TIMESTAMPTZ NOT NULL,
  fin TIMESTAMPTZ,
  langue TEXT,
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_seance_salle_debut ON seance(id_salle, debut);

-- ------------------------------
-- PERSONNES
-- ------------------------------
CREATE TABLE personne (
  id SERIAL PRIMARY KEY,
  nom_complet TEXT,
  email TEXT UNIQUE,
  telephone TEXT,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- STATUS (pour tickets et reservations)
-- ------------------------------
CREATE TABLE status (
  id SERIAL PRIMARY KEY,
  code TEXT UNIQUE NOT NULL,    -- CREE, EN_ATTENTE, PAYE, ANNULE...
  libelle TEXT NOT NULL,
  valeur INT NOT NULL,          -- hiérarchie / tri, ex: CREE=1, EN_ATTENTE=11, PAYE=21
  est_final BOOLEAN DEFAULT false
);

-- ------------------------------
-- RESERVATIONS
-- ------------------------------
CREATE TABLE reservation (
  id SERIAL PRIMARY KEY,
  id_personne INT REFERENCES personne(id) NULL,
  id_seance INT REFERENCES seance(id),
  id_status INT REFERENCES status(id),
  montant_total NUMERIC(6,2) DEFAULT 0,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- HISTORIQUE STATUT RESERVATION
-- ------------------------------
CREATE TABLE historique_statut_reservation (
  id SERIAL PRIMARY KEY,
  id_reservation INT REFERENCES reservation(id) ON DELETE CASCADE,
  id_status INT REFERENCES status(id),
  date_changement TIMESTAMPTZ DEFAULT now(),
  change_par INT REFERENCES personne(id),
  commentaire TEXT
);

-- ------------------------------
-- TICKETS
-- ------------------------------
CREATE TABLE ticket (
  id SERIAL PRIMARY KEY,
  id_reservation INT REFERENCES reservation(id) NULL,
  id_seance INT REFERENCES seance(id),
  id_place INT REFERENCES place(id),
  id_status INT REFERENCES status(id),
  id_categorie_personne INT REFERENCES categorie_personne(id),
  prix NUMERIC(6,2) NOT NULL,
  cree_le TIMESTAMPTZ DEFAULT now(),
  UNIQUE (id_seance, id_place)
);

-- ------------------------------
-- HISTORIQUE STATUT TICKET
-- ------------------------------
CREATE TABLE historique_statut_ticket (
  id SERIAL PRIMARY KEY,
  id_ticket INT REFERENCES ticket(id) ON DELETE CASCADE,
  id_status INT REFERENCES status(id),
  date_changement TIMESTAMPTZ DEFAULT now(),
  change_par INT REFERENCES personne(id),
  commentaire TEXT
);

-- ------------------------------
-- TARIF PAR DEFAUT
-- ------------------------------
CREATE TABLE tarif_defaut (
  id SERIAL PRIMARY KEY,
  id_type_place INT REFERENCES type_place(id),
  id_categorie_personne INT REFERENCES categorie_personne(id),
  prix NUMERIC(6,2) NOT NULL
);

-- ------------------------------
-- TARIF SPECIFIQUE PAR SEANCE (OPTIONNEL)
-- ------------------------------
CREATE TABLE tarif_seance (
  id SERIAL PRIMARY KEY,
  id_seance INT REFERENCES seance(id),
  id_type_place INT REFERENCES type_place(id),
  id_categorie_personne INT REFERENCES categorie_personne(id),
  prix NUMERIC(6,2) NOT NULL
);

-- ------------------------------
-- EXEMPLES STATUS INITIAUX
-- ------------------------------
INSERT INTO status (code, libelle, valeur, est_final) VALUES
  ('CREE','Creee',1,false),
  ('EN_ATTENTE','En attente de paiement',11,false),
  ('PAYE','Payee',21,false),
  ('CONFIRMEE','Confirmee',31,false),
  ('ANNULEE','Annulee',100,true),
  ('EXPIREE','Expiree',101,true);

-- Exemple de categories
INSERT INTO categorie_personne (libelle) VALUES
  ('ADULTE'),('ENFANT'),('SENIOR');

-- Exemple de types de place
INSERT INTO type_place (libelle) VALUES
  ('STANDARD'),('VIP'),('PMR');
