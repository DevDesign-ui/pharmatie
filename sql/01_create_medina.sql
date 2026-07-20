-- =====================================================================
-- PharmaSante - Base de la Pharmacie MEDINA (MySQL / XAMPP)
-- =====================================================================
-- A executer depuis phpMyAdmin (onglet SQL) ou en ligne de commande :
--   mysql -u root < sql/01_create_medina.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS pharmacie_medina
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacie_medina;

-- (Optionnel) Utilisateur dedie
-- CREATE USER 'pharma_medina'@'localhost' IDENTIFIED BY 'medina';
-- GRANT ALL PRIVILEGES ON pharmacie_medina.* TO 'pharma_medana'@'localhost';

-- =====================================================================
-- Table des medicaments
-- =====================================================================
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
    UNIQUE KEY uk_medina_code (code)
);

CREATE INDEX idx_medina_categorie  ON medicaments(categorie);
CREATE INDEX idx_medina_expiration ON medicaments(date_expiration);

-- =====================================================================
-- Donnees initiales (exemples)
-- =====================================================================
INSERT INTO medicaments (code, nom, description, categorie, prix, quantite,
                         date_fabrication, date_expiration, created_at, updated_at)
VALUES
    ('MED-001', 'Paracetamol 500mg', 'Antidouleur et antipyretique', 'Antidouleur',
     1500, 120, '2024-01-15', '2026-12-31', NOW(), NOW()),
    ('MED-002', 'Amoxicilline 250mg', 'Antibiotique a large spectre', 'Antibiotique',
     3200, 60, '2024-03-10', '2027-03-09', NOW(), NOW()),
    ('MED-003', 'Ibuprofene 400mg', 'Anti-inflammatoire non steroidien', 'AINS',
     2100, 80, '2024-02-20', '2027-02-19', NOW(), NOW());
