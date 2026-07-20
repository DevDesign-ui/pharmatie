-- =====================================================================
-- PharmaSante - Base de la Pharmacie ALMADIES (MySQL / XAMPP)
-- =====================================================================
-- A executer depuis phpMyAdmin (onglet SQL) ou en ligne de commande :
--   mysql -u root < sql/03_create_almadies.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS pharmacie_almadies
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacie_almadies;

-- (Optionnel) Utilisateur dedie
-- CREATE USER 'pharma_almadies'@'localhost' IDENTIFIED BY 'almadies';
-- GRANT ALL PRIVILEGES ON pharmacie_almadies.* TO 'pharma_almadies'@'localhost';

CREATE TABLE IF NOT EXISTS medicaments (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    code              VARCHAR(50)     NOT NULL,
    nom               VARCHAR(150)    NOT NULL,
    description       VARCHAR(500)    NULL,
    categorie         VARCHAR(80)     NULL,
    prix              DECIMAL(12, 2)  NOT NULL,
    quantite          INT             NOT NULL,
    date_fabrication  DATE            NULL,
    date_expiration   DATE            NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_almadies_code (code)
);

CREATE INDEX idx_almadies_categorie  ON medicaments(categorie);
CREATE INDEX idx_almadies_expiration ON medicaments(date_expiration);

INSERT INTO medicaments (code, nom, description, categorie, prix, quantite,
                         date_fabrication, date_expiration, created_at, updated_at)
VALUES
    ('ALM-001', 'Metformine 500mg', 'Antidiabetique oral', 'Antidiabetique',
     4500, 35, '2024-05-12', '2027-05-11', NOW(), NOW()),
    ('MED-001', 'Paracetamol 500mg', 'Antidouleur - version Almadies', 'Antidouleur',
     1600, 90, '2024-01-15', '2026-12-31', NOW(), DATE_ADD(NOW(), INTERVAL 2 MINUTE)),
    ('LIB-001', 'Aspirine 100mg', 'Anticoagulant - copie Almadies', 'Antidouleur',
     950, 50, '2024-04-01', '2027-04-01', NOW(), NOW());
