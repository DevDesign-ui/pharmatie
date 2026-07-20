package com.pharmasante.pharmadb.repository.medina;

import com.pharmasante.pharmadb.entity.medina.MedicamentMedina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA de la Pharmacie Medina.
 * Relie a l'EntityManagerFactory dedie a la base de Medina.
 */
@Repository
public interface MedicamentMedinaRepository extends JpaRepository<MedicamentMedina, Long> {

    Optional<MedicamentMedina> findByCode(String code);

    boolean existsByCode(String code);
}
