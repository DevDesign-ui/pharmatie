-- =====================================================================
-- PharmaSante - Base de la Pharmacie LIBERTE (MySQL / XAMPP)
-- =====================================================================
-- A executer depuis phpMyAdmin (onglet SQL) ou en ligne de commande :
--   mysql -u root < sql/02_create_liberte.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS pharmacie_liberte
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacie_liberte;

-- (Optionnel) Utilisateur dedie
-- CREATE USER 'pharma_liberte'@'localhost' IDENTIFIED BY 'liberte';
-- GRANT ALL PRIVILEGES ON pharmacie_liberte.* TO 'pharma_liberte'@'localhost';

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
    UNIQUE KEY uk_liberte_code (code)
);

CREATE INDEX idx_liberte_categorie  ON medicaments(categorie);
CREATE INDEX idx_liberte_expiration ON medicaments(date_expiration);

INSERT INTO medicaments (code, nom, description, categorie, prix, quantite,
                         date_fabrication, date_expiration, created_at, updated_at)
VALUES
    ('LIB-001', 'Aspirine 100mg', 'Anticoagulant et antipyretique', 'Antidouleur',
     900, 200, '2024-04-01', '2027-04-01', NOW(), NOW()),
    ('MED-002', 'Amoxicilline 250mg', 'Antibiotique - version modifiee', 'Antibiotique',
     3500, 40, '2024-03-10', '2027-03-09', NOW(), DATE_ADD(NOW(), INTERVAL 1 MINUTE));
