-- ------------------------------
-- SOCIÉTÉS ANNONCEURS
-- ------------------------------
CREATE TABLE societe (
  id SERIAL PRIMARY KEY,
  nom TEXT NOT NULL,
  contact TEXT,
  email TEXT,
  telephone TEXT,
  actif BOOLEAN DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- VIDÉOS PUBLICITAIRES (catalogue)
-- ------------------------------
CREATE TABLE video_publicitaire (
  id SERIAL PRIMARY KEY,
  id_societe INT NOT NULL REFERENCES societe(id),
  titre TEXT NOT NULL,
  duree_secondes INT NOT NULL CHECK (duree_secondes > 0),
  date_creation DATE,
  actif BOOLEAN DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_video_publicitaire_societe ON video_publicitaire(id_societe);

-- ------------------------------
-- GRILLE TARIFAIRE PUBLICITÉ
-- ------------------------------
CREATE TABLE tarif_publicite (
  id SERIAL PRIMARY KEY,
  prix_par_diffusion NUMERIC(12,2) NOT NULL,
  date_debut DATE NOT NULL,
  date_fin DATE,
  actif BOOLEAN DEFAULT true,
  description TEXT,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- ------------------------------
-- CONTRAT PUBLICITÉ (équivalent reservation)
-- ------------------------------
CREATE TABLE contrat_publicite (
  id SERIAL PRIMARY KEY,
  id_societe INT NOT NULL REFERENCES societe(id),
  id_tarif_publicite INT NOT NULL REFERENCES tarif_publicite(id),
  nb_diffusions INT NOT NULL DEFAULT 0 CHECK (nb_diffusions >= 0),
  montant_total NUMERIC(12,2) NOT NULL DEFAULT 0,
  date_debut DATE NOT NULL,
  date_fin DATE,
  date_contrat TIMESTAMPTZ DEFAULT now(),
  actif BOOLEAN DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_contrat_publicite_societe ON contrat_publicite(id_societe);
CREATE INDEX idx_contrat_publicite_dates ON contrat_publicite(date_debut, date_fin);

-- ------------------------------
-- DIFFUSIONS RÉELLES (équivalent details_reservation)
-- ------------------------------
CREATE TABLE diffusion_publicite (
  id SERIAL PRIMARY KEY,
  id_contrat_publicite INT NOT NULL REFERENCES contrat_publicite(id) ON DELETE CASCADE,
  id_seance INT NOT NULL REFERENCES seance(id),
  id_video_publicitaire INT NOT NULL REFERENCES video_publicitaire(id),
  ordre_diffusion INT, -- ordre d'apparition avant le film (1er, 2ème, etc.)
  prix_unitaire NUMERIC(12,2) NOT NULL, -- prix de cette diffusion
  is_diffusee BOOLEAN DEFAULT false, -- la pub a-t-elle été effectivement diffusée ?
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_diffusion_publicite_contrat ON diffusion_publicite(id_contrat_publicite);
CREATE INDEX idx_diffusion_publicite_seance ON diffusion_publicite(id_seance);
CREATE INDEX idx_diffusion_publicite_video ON diffusion_publicite(id_video_publicitaire);

INSERT INTO societe (id, nom, contact, email, telephone, actif) VALUES
  (1, 'VANIALA', 'Contact Vaniala', 'contact@vaniala.mg', '0340000001', true),
  (2, 'LEWIS', 'Contact Lewis', 'contact@lewis.mg', '0340000002', true);

SELECT setval('societe_id_seq', (SELECT COALESCE(MAX(id), 1) FROM societe));

INSERT INTO video_publicitaire (id, id_societe, titre, duree_secondes, date_creation, actif) VALUES
  (1, 1, 'VANIALA - Spot 1', 30, '2025-11-15', true),
  (2, 1, 'VANIALA - Spot 2', 25, '2025-11-20', true),
  (3, 2, 'LEWIS - Spot 1', 20, '2025-11-18', true);

SELECT setval('video_publicitaire_id_seq', (SELECT COALESCE(MAX(id), 1) FROM video_publicitaire));

INSERT INTO tarif_publicite (id, prix_par_diffusion, date_debut, date_fin, actif, description) VALUES
  (1, 200000, '2025-01-01', NULL, true, 'Tarif standard'),
  (2, 250000, '2025-12-15', '2025-12-31', true, 'Tarif spécial fin d''année');

SELECT setval('tarif_publicite_id_seq', (SELECT COALESCE(MAX(id), 1) FROM tarif_publicite));

INSERT INTO contrat_publicite (id, id_societe, id_tarif_publicite, nb_diffusions, montant_total, date_debut, date_fin, actif) VALUES
  (1, 1, 1, 20, 0, '2025-12-01', '2025-12-31', true),
  (2, 2, 1, 10, 0, '2025-12-01', '2025-12-31', true);

SELECT setval('contrat_publicite_id_seq', (SELECT COALESCE(MAX(id), 1) FROM contrat_publicite));

INSERT INTO seance (id, id_film, id_salle, date_heure, langue, version) VALUES
  (3, 1, 1, '2025-12-05 18:00:00+01', 'Français', 'VF'),
  (4, 1, 2, '2025-12-10 18:00:00+01', 'Français', 'VF'),
  (5, 1, 2, '2025-12-20 20:00:00+01', 'Français', 'VOST'),
  (6, 1, 1, '2025-12-28 16:00:00+01', 'Français', 'VF');

SELECT setval('seance_id_seq', (SELECT COALESCE(MAX(id), 1) FROM seance));

















INSERT INTO diffusion_publicite (id_contrat_publicite, id_seance, id_video_publicitaire, ordre_diffusion, prix_unitaire, is_diffusee)
SELECT
  1,
  CASE
    WHEN gs <= 7 THEN 3
    WHEN gs <= 14 THEN 4
    ELSE 5
  END,
  CASE WHEN (gs % 2) = 0 THEN 1 ELSE 2 END,
  gs,
  CASE
    WHEN gs IN (15, 16, 17, 18) THEN 250000
    ELSE 200000
  END,
  true
FROM generate_series(1, 20) gs;

INSERT INTO diffusion_publicite (id_contrat_publicite, id_seance, id_video_publicitaire, ordre_diffusion, prix_unitaire, is_diffusee)
SELECT
  2,
  CASE
    WHEN gs <= 4 THEN 4
    WHEN gs <= 7 THEN 5
    ELSE 6
  END,
  3,
  gs,
  CASE
    WHEN gs IN (1, 2) THEN 180000
    ELSE 200000
  END,
  true
FROM generate_series(1, 10) gs;

SELECT setval('diffusion_publicite_id_seq', (SELECT COALESCE(MAX(id), 1) FROM diffusion_publicite));