package com.pharmasante.pharmadb.model;

/**
 * Enumeration representant les trois pharmacies de la chaine PharmaSante.
 * Utilisee pour identifier la provenance d'un medicament lors de la
 * consolidation et de la synchronisation.
 */
public enum Pharmacie {

    MEDINA("Pharmacie Medina"),
    LIBERTE("Pharmacie Liberte"),
    ALMADIES("Pharmacie Almadies");

    private final String libelle;

    Pharmacie(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
