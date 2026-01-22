-- Sociétés annonceurs
CREATE TABLE societe (
  id SERIAL PRIMARY KEY,
  nom TEXT NOT NULL,
  contact TEXT,
  email TEXT,
  telephone TEXT,
  actif BOOLEAN DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- Vidéos publicitaires (catalogue)
CREATE TABLE video_publicitaire (
  id SERIAL PRIMARY KEY,
  id_societe INT NOT NULL REFERENCES societe(id),
  titre TEXT NOT NULL,
  duree_secondes INT NOT NULL CHECK (duree_secondes > 0),
  date_creation DATE,
  actif BOOLEAN DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- Grille tarifaire pour les publicités
CREATE TABLE tarif_publicite (
  id SERIAL PRIMARY KEY,
  prix_par_diffusion NUMERIC(12,2) NOT NULL,
  date_debut DATE NOT NULL,
  date_fin DATE,
  actif BOOLEAN DEFAULT true,
  description TEXT, -- ex: "Tarif haute saison"
  cree_le TIMESTAMPTZ DEFAULT now()
);

-- Diffusions réelles des publicités
CREATE TABLE diffusion_publicite (
  id SERIAL PRIMARY KEY,
  id_seance INT NOT NULL REFERENCES seance(id),
  id_video_publicitaire INT NOT NULL REFERENCES video_publicitaire(id),
  id_tarif_publicite INT NOT NULL REFERENCES tarif_publicite(id),
  ordre_diffusion INT, -- ordre d'apparition avant le film
  prix_applique NUMERIC(12,2) NOT NULL, -- prix au moment de la diffusion
  date_diffusion TIMESTAMPTZ NOT NULL, -- redondant avec seance mais utile pour les rapports
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_diffusion_seance ON diffusion_publicite(id_seance);
CREATE INDEX idx_diffusion_video ON diffusion_publicite(id_video_publicitaire);
CREATE INDEX idx_diffusion_date ON diffusion_publicite(date_diffusion);