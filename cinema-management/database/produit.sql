
CREATE TABLE produit_extra (
  id SERIAL PRIMARY KEY,
  nom TEXT NOT NULL UNIQUE,
  description TEXT,
  actif BOOLEAN NOT NULL DEFAULT true,
  cree_le TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE produit_tarif (
  id SERIAL PRIMARY KEY,
  id_produit_extra INT NOT NULL REFERENCES produit_extra(id) ON DELETE CASCADE,
  prix NUMERIC(12,2) NOT NULL CHECK (prix >= 0),
  date_debut DATE NOT NULL,
  date_fin DATE,
  CHECK (date_fin IS NULL OR date_fin >= date_debut),
  UNIQUE (id_produit_extra, date_debut)
);

CREATE INDEX idx_produit_tarif_produit ON produit_tarif(id_produit_extra);
CREATE INDEX idx_produit_tarif_periode ON produit_tarif(id_produit_extra, date_debut, date_fin);

INSERT INTO produit_extra (nom, description, actif)
VALUES ('Pop corn', 'Pop corn', true)
ON CONFLICT (nom) DO NOTHING;

INSERT INTO produit_tarif (id_produit_extra, prix, date_debut, date_fin)
VALUES (
  (SELECT id FROM produit_extra WHERE nom = 'Pop corn'),
  10000,
  CURRENT_DATE,
  NULL
)
ON CONFLICT (id_produit_extra, date_debut) DO NOTHING;

CREATE TABLE vente (
  id SERIAL PRIMARY KEY,
  id_client INT REFERENCES client(id),
  date_vente TIMESTAMPTZ NOT NULL DEFAULT now(),
  montant_total NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (montant_total >= 0)
);

CREATE INDEX idx_vente_date_vente ON vente(date_vente);
CREATE INDEX idx_vente_client ON vente(id_client);

CREATE TABLE vente_ligne (
  id SERIAL PRIMARY KEY,
  id_vente INT NOT NULL REFERENCES vente(id) ON DELETE CASCADE,
  id_produit_extra INT NOT NULL REFERENCES produit_extra(id),
  quantite INT NOT NULL CHECK (quantite > 0),
  prix_unitaire NUMERIC(12,2) NOT NULL CHECK (prix_unitaire >= 0),
  montant_ligne NUMERIC(12,2) GENERATED ALWAYS AS ((quantite::numeric) * prix_unitaire) STORED,
  UNIQUE (id_vente, id_produit_extra)
);

CREATE INDEX idx_vente_ligne_vente ON vente_ligne(id_vente);
CREATE INDEX idx_vente_ligne_produit ON vente_ligne(id_produit_extra);

CREATE OR REPLACE VIEW v_ca_vente_par_mois AS
SELECT
  date_trunc('month', v.date_vente) AS mois,
  SUM(v.montant_total) AS ca
FROM vente v
GROUP BY date_trunc('month', v.date_vente)
ORDER BY mois;

SELECT
  SUM(v.montant_total) AS ca_janvier_2025
FROM vente v
WHERE v.date_vente >= TIMESTAMPTZ '2025-01-01 00:00:00+00'
  AND v.date_vente <  TIMESTAMPTZ '2025-02-01 00:00:00+00';
