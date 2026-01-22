-- ------------------------------
-- SOCIÉTÉS ANNONCEURS
-- ------------------------------
CREATE TABLE societe (
  id SERIAL PRIMARY KEY,
  nom TEXT NOT NULL,
  contact TEXT,
  email TEXT,
  telephone TEXT,
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
  id_tarif_publicite INT NOT NULL REFERENCES tarif_publicite(id),
  id_video_publicitaire INT NOT NULL REFERENCES video_publicitaire(id),
  nb_diffusions INT NOT NULL DEFAULT 0 CHECK (nb_diffusions >= 0),
  montant_total NUMERIC(12,2) NOT NULL DEFAULT 0,
  date_debut DATE NOT NULL,
  date_fin DATE,
  date_contrat TIMESTAMPTZ DEFAULT now(),
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_contrat_publicite_video ON contrat_publicite(id_video_publicitaire);
CREATE INDEX idx_contrat_publicite_dates ON contrat_publicite(date_debut, date_fin);

-- ------------------------------
-- DIFFUSIONS RÉELLES (équivalent details_reservation)
-- ------------------------------
CREATE TABLE diffusion_publicite (
  id SERIAL PRIMARY KEY,
  id_contrat_publicite INT NOT NULL REFERENCES contrat_publicite(id) ON DELETE CASCADE,
  id_seance INT NOT NULL REFERENCES seance(id),
  nombre_pub INT NOT NULL DEFAULT 0 CHECK (nombre_pub >= 0),
  cree_le TIMESTAMPTZ DEFAULT now()
);

ALTER TABLE diffusion_publicite
  ADD CONSTRAINT uq_diffusion_publicite_contrat_seance UNIQUE (id_contrat_publicite, id_seance);

CREATE INDEX idx_diffusion_publicite_contrat ON diffusion_publicite(id_contrat_publicite);
CREATE INDEX idx_diffusion_publicite_seance ON diffusion_publicite(id_seance);

CREATE TABLE paiement_contrat_publicite (
  id SERIAL PRIMARY KEY,
  id_contrat_publicite INT NOT NULL REFERENCES contrat_publicite(id) ON DELETE CASCADE,
  date_paiement TIMESTAMPTZ NOT NULL DEFAULT now(),
  montant NUMERIC(12,2) NOT NULL CHECK (montant > 0),
  mode TEXT,
  reference TEXT,
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_paiement_contrat_publicite_contrat ON paiement_contrat_publicite(id_contrat_publicite);
CREATE INDEX idx_paiement_contrat_publicite_date ON paiement_contrat_publicite(date_paiement);

INSERT INTO societe (nom, contact, email, telephone) VALUES
  ('Vaniala', 'Responsable marketing', 'contact@vaniala.mg', '0340000001'),
  ('Lewis', 'Service communication', 'contact@lewis.mg', '0340000002');

INSERT INTO video_publicitaire (id_societe, titre, duree_secondes, date_creation) VALUES
  ((SELECT id FROM societe WHERE nom = 'Vaniala'), 'Vaniala - Spot 15s', 15, '2025-11-15'),
  ((SELECT id FROM societe WHERE nom = 'Vaniala'), 'Vaniala - Spot 30s', 30, '2025-11-20'),
  ((SELECT id FROM societe WHERE nom = 'Lewis'), 'Lewis - Spot 20s', 20, '2025-11-10'),
  ((SELECT id FROM societe WHERE nom = 'Lewis'), 'Lewis - Spot 10s', 10, '2025-11-12');

INSERT INTO tarif_publicite (prix_par_diffusion, date_debut, date_fin, actif, description) VALUES
  (200000, '2025-01-01', NULL, true, 'Tarif standard');


