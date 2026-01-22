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