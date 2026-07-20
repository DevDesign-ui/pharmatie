package com.pharmasante.pharmadb.entity.medina;

import com.pharmasante.pharmadb.entity.AbstractMedicament;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entite concrete representant un medicament stocke dans la base de donnees
 * de la Pharmacie Medina.
 */
@Entity
@Table(name = "medicaments")
public class MedicamentMedina extends AbstractMedicament {
}
