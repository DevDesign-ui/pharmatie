package com.pharmasante.pharmadb.entity.almadies;

import com.pharmasante.pharmadb.entity.AbstractMedicament;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entite concrete representant un medicament stocke dans la base de donnees
 * de la Pharmacie Almadies.
 */
@Entity
@Table(name = "medicaments")
public class MedicamentAlmadies extends AbstractMedicament {
}
