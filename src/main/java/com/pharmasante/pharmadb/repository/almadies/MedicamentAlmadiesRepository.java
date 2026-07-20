package com.pharmasante.pharmadb.repository.almadies;

import com.pharmasante.pharmadb.entity.almadies.MedicamentAlmadies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA de la Pharmacie Almadies.
 * Relie a l'EntityManagerFactory dedie a la base des Almadies.
 */
@Repository
public interface MedicamentAlmadiesRepository extends JpaRepository<MedicamentAlmadies, Long> {

    Optional<MedicamentAlmadies> findByCode(String code);

    boolean existsByCode(String code);
}
