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

INSERT INTO societe (nom, contact, email, telephone, actif) VALUES
  ('Vaniala', 'Responsable marketing', 'contact@vaniala.mg', '0340000001', true),
  ('Lewis', 'Service communication', 'contact@lewis.mg', '0340000002', true);

INSERT INTO video_publicitaire (id_societe, titre, duree_secondes, date_creation, actif) VALUES
  ((SELECT id FROM societe WHERE nom = 'Vaniala'), 'Vaniala - Spot 15s', 15, '2025-11-15', true),
  ((SELECT id FROM societe WHERE nom = 'Vaniala'), 'Vaniala - Spot 30s', 30, '2025-11-20', true),
  ((SELECT id FROM societe WHERE nom = 'Lewis'), 'Lewis - Spot 20s', 20, '2025-11-10', true),
  ((SELECT id FROM societe WHERE nom = 'Lewis'), 'Lewis - Spot 10s', 10, '2025-11-12', true);

INSERT INTO tarif_publicite (prix_par_diffusion, date_debut, date_fin, actif, description) VALUES
  (200000, '2025-01-01', NULL, true, 'Tarif standard');

INSERT INTO seance (id_film, id_salle, date_heure, langue, version) VALUES
  (1, 1, '2025-12-05 18:00:00+03', 'Français', 'VF'),
  (1, 1, '2025-12-12 18:00:00+03', 'Français', 'VF'),
  (1, 2, '2025-12-20 20:00:00+03', 'Français', 'VOST'),
  (1, 2, '2025-12-28 16:00:00+03', 'Français', 'VF');

INSERT INTO contrat_publicite (
  id_societe,
  id_tarif_publicite,
  nb_diffusions,
  montant_total,
  date_debut,
  date_fin,
  actif
) VALUES
  (
    (SELECT id FROM societe WHERE nom = 'Vaniala'),
    (SELECT id FROM tarif_publicite WHERE actif = true ORDER BY date_debut DESC LIMIT 1),
    20,
    4000000,
    '2025-12-01',
    '2025-12-31',
    true
  ),
  (
    (SELECT id FROM societe WHERE nom = 'Lewis'),
    (SELECT id FROM tarif_publicite WHERE actif = true ORDER BY date_debut DESC LIMIT 1),
    10,
    2000000,
    '2025-12-01',
    '2025-12-31',
    true
  );

DO $$
DECLARE
  seance_ids INT[];
  i INT;
  contrat_id INT;
  vid15 INT;
  vid30 INT;
  prix NUMERIC(12,2);
  s_id INT;
BEGIN
  contrat_id := (SELECT id FROM contrat_publicite cp JOIN societe s ON s.id = cp.id_societe WHERE s.nom = 'Vaniala' ORDER BY cp.id DESC LIMIT 1);
  vid15 := (SELECT id FROM video_publicitaire vp JOIN societe s ON s.id = vp.id_societe WHERE s.nom = 'Vaniala' AND vp.titre = 'Vaniala - Spot 15s' LIMIT 1);
  vid30 := (SELECT id FROM video_publicitaire vp JOIN societe s ON s.id = vp.id_societe WHERE s.nom = 'Vaniala' AND vp.titre = 'Vaniala - Spot 30s' LIMIT 1);
  seance_ids := ARRAY(SELECT id FROM seance WHERE date_heure >= '2025-12-01' AND date_heure < '2026-01-01' ORDER BY date_heure);

  FOR i IN 1..20 LOOP
    s_id := seance_ids[((i - 1) % array_length(seance_ids, 1)) + 1];
    IF i <= 5 THEN
      prix := 180000;
    ELSIF i <= 10 THEN
      prix := 220000;
    ELSE
      prix := 200000;
    END IF;

    INSERT INTO diffusion_publicite (id_contrat_publicite, id_seance, id_video_publicitaire, ordre_diffusion, prix_unitaire, is_diffusee)
    VALUES (contrat_id, s_id, CASE WHEN (i % 2) = 0 THEN vid30 ELSE vid15 END, (i % 3) + 1, prix, true);
  END LOOP;
END $$;

DO $$
DECLARE
  seance_ids INT[];
  i INT;
  contrat_id INT;
  vid20 INT;
  vid10 INT;
  prix NUMERIC(12,2);
  s_id INT;
BEGIN
  contrat_id := (SELECT id FROM contrat_publicite cp JOIN societe s ON s.id = cp.id_societe WHERE s.nom = 'Lewis' ORDER BY cp.id DESC LIMIT 1);
  vid20 := (SELECT id FROM video_publicitaire vp JOIN societe s ON s.id = vp.id_societe WHERE s.nom = 'Lewis' AND vp.titre = 'Lewis - Spot 20s' LIMIT 1);
  vid10 := (SELECT id FROM video_publicitaire vp JOIN societe s ON s.id = vp.id_societe WHERE s.nom = 'Lewis' AND vp.titre = 'Lewis - Spot 10s' LIMIT 1);
  seance_ids := ARRAY(SELECT id FROM seance WHERE date_heure >= '2025-12-01' AND date_heure < '2026-01-01' ORDER BY date_heure);

  FOR i IN 1..10 LOOP
    s_id := seance_ids[((i - 1) % array_length(seance_ids, 1)) + 1];
    IF i <= 3 THEN
      prix := 210000;
    ELSIF i <= 6 THEN
      prix := 190000;
    ELSE
      prix := 200000;
    END IF;

    INSERT INTO diffusion_publicite (id_contrat_publicite, id_seance, id_video_publicitaire, ordre_diffusion, prix_unitaire, is_diffusee)
    VALUES (contrat_id, s_id, CASE WHEN (i % 2) = 0 THEN vid10 ELSE vid20 END, (i % 4) + 1, prix, true);
  END LOOP;
END $$;