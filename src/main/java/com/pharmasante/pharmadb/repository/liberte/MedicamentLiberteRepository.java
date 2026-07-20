package com.pharmasante.pharmadb.repository.liberte;

import com.pharmasante.pharmadb.entity.liberte.MedicamentLiberte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA de la Pharmacie Liberte.
 * Relie a l'EntityManagerFactory dedie a la base de Liberte.
 */
@Repository
public interface MedicamentLiberteRepository extends JpaRepository<MedicamentLiberte, Long> {

    Optional<MedicamentLiberte> findByCode(String code);

    boolean existsByCode(String code);
}
