package com.pharmasante.pharmadb.service;

import com.pharmasante.pharmadb.config.AlmadiesConfig;
import com.pharmasante.pharmadb.config.LiberteConfig;
import com.pharmasante.pharmadb.config.MedinaConfig;
import com.pharmasante.pharmadb.entity.AbstractMedicament;
import com.pharmasante.pharmadb.entity.almadies.MedicamentAlmadies;
import com.pharmasante.pharmadb.entity.liberte.MedicamentLiberte;
import com.pharmasante.pharmadb.entity.medina.MedicamentMedina;
import com.pharmasante.pharmadb.mapper.MedicamentMapper;
import com.pharmasante.pharmadb.model.MedicamentDto;
import com.pharmasante.pharmadb.model.Pharmacie;
import com.pharmasante.pharmadb.repository.almadies.MedicamentAlmadiesRepository;
import com.pharmasante.pharmadb.repository.liberte.MedicamentLiberteRepository;
import com.pharmasante.pharmadb.repository.medina.MedicamentMedinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service central d'acces aux stocks des trois pharmacies.
 *
 * Chaque operation est executee dans la transaction de la pharmacie concernee
 * (grace a l'attribut {@code transactionManager} de {@code @Transactional}),
 * ce qui permet de manipuler plusieurs bases de donnees independantes dans une
 * meme application sans recourir a une transaction distribuee (XA).
 *
 * Responsabilites :
 *  - ajout d'un medicament dans une pharmacie ;
 *  - modification d'un medicament ;
 *  - suppression d'un medicament ;
 *  - consultation du stock d'une pharmacie ;
 *  - recherche d'un medicament par code ;
 *  - consolidation des stocks des trois pharmacies.
 */
@Service
public class MultiStockService {

    private final MedicamentMedinaRepository   medinaRepo;
    private final MedicamentLiberteRepository  liberteRepo;
    private final MedicamentAlmadiesRepository almadiesRepo;

    public MultiStockService(MedicamentMedinaRepository medinaRepo,
                             MedicamentLiberteRepository liberteRepo,
                             MedicamentAlmadiesRepository almadiesRepo) {
        this.medinaRepo   = medinaRepo;
        this.liberteRepo  = liberteRepo;
        this.almadiesRepo = almadiesRepo;
    }

    // =================================================================
    //  Consultation : stock d'une pharmacie
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM, readOnly = true)
    public List<MedicamentDto> listerMedina() {
        return medinaRepo.findAll().stream()
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.MEDINA))
                .toList();
    }

    @Transactional(transactionManager = LiberteConfig.TM, readOnly = true)
    public List<MedicamentDto> listerLiberte() {
        return liberteRepo.findAll().stream()
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.LIBERTE))
                .toList();
    }

    @Transactional(transactionManager = AlmadiesConfig.TM, readOnly = true)
    public List<MedicamentDto> listerAlmadies() {
        return almadiesRepo.findAll().stream()
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.ALMADIES))
                .toList();
    }

    public List<MedicamentDto> listerParPharmacie(Pharmacie pharmacie) {
        return switch (pharmacie) {
            case MEDINA   -> listerMedina();
            case LIBERTE  -> listerLiberte();
            case ALMADIES -> listerAlmadies();
        };
    }

    // =================================================================
    //  Vue consolidee : fusion des stocks des trois pharmacies
    // =================================================================

    public List<MedicamentDto> stockConsolide() {
        List<MedicamentDto> all = new ArrayList<>();
        all.addAll(listerMedina());
        all.addAll(listerLiberte());
        all.addAll(listerAlmadies());
        return all;
    }

    // =================================================================
    //  Recherche par code
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM, readOnly = true)
    public Optional<MedicamentDto> rechercherMedina(String code) {
        return medinaRepo.findByCode(code)
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.MEDINA));
    }

    @Transactional(transactionManager = LiberteConfig.TM, readOnly = true)
    public Optional<MedicamentDto> rechercherLiberte(String code) {
        return liberteRepo.findByCode(code)
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.LIBERTE));
    }

    @Transactional(transactionManager = AlmadiesConfig.TM, readOnly = true)
    public Optional<MedicamentDto> rechercherAlmadies(String code) {
        return almadiesRepo.findByCode(code)
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.ALMADIES));
    }

    /**
     * Recherche un medicament par code dans toutes les pharmacies.
     * Renvoie la liste des occurrences trouvees (une pharmacie peut avoir le
     * medicament alors qu'une autre ne l'a pas).
     */
    public List<MedicamentDto> rechercherPartout(String code) {
        List<MedicamentDto> resultats = new ArrayList<>();
        rechercherMedina(code).ifPresent(resultats::add);
        rechercherLiberte(code).ifPresent(resultats::add);
        rechercherAlmadies(code).ifPresent(resultats::add);
        return resultats;
    }

    // =================================================================
    //  Ajout
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM)
    public MedicamentDto ajouterMedina(MedicamentDto dto) {
        if (medinaRepo.existsByCode(dto.getCode())) {
            throw new IllegalStateException(
                    "Un medicament avec le code " + dto.getCode() + " existe deja a Medina.");
        }
        MedicamentMedina entity = MedicamentMapper.newMedinaEntity(dto);
        return MedicamentMapper.toDto(medinaRepo.save(entity), Pharmacie.MEDINA);
    }

    @Transactional(transactionManager = LiberteConfig.TM)
    public MedicamentDto ajouterLiberte(MedicamentDto dto) {
        if (liberteRepo.existsByCode(dto.getCode())) {
            throw new IllegalStateException(
                    "Un medicament avec le code " + dto.getCode() + " existe deja a Liberte.");
        }
        MedicamentLiberte entity = MedicamentMapper.newLiberteEntity(dto);
        return MedicamentMapper.toDto(liberteRepo.save(entity), Pharmacie.LIBERTE);
    }

    @Transactional(transactionManager = AlmadiesConfig.TM)
    public MedicamentDto ajouterAlmadies(MedicamentDto dto) {
        if (almadiesRepo.existsByCode(dto.getCode())) {
            throw new IllegalStateException(
                    "Un medicament avec le code " + dto.getCode() + " existe deja aux Almadies.");
        }
        MedicamentAlmadies entity = MedicamentMapper.newAlmadiesEntity(dto);
        return MedicamentMapper.toDto(almadiesRepo.save(entity), Pharmacie.ALMADIES);
    }

    public MedicamentDto ajouter(MedicamentDto dto) {
        return switch (dto.getPharmacie()) {
            case MEDINA   -> ajouterMedina(dto);
            case LIBERTE  -> ajouterLiberte(dto);
            case ALMADIES -> ajouterAlmadies(dto);
        };
    }

    // =================================================================
    //  Modification
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM)
    public MedicamentDto modifierMedina(Long id, MedicamentDto dto) {
        MedicamentMedina existing = medinaRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable a Medina : " + id));
        MedicamentMapper.copyDtoToEntity(dto, existing);
        return MedicamentMapper.toDto(medinaRepo.save(existing), Pharmacie.MEDINA);
    }

    @Transactional(transactionManager = LiberteConfig.TM)
    public MedicamentDto modifierLiberte(Long id, MedicamentDto dto) {
        MedicamentLiberte existing = liberteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable a Liberte : " + id));
        MedicamentMapper.copyDtoToEntity(dto, existing);
        return MedicamentMapper.toDto(liberteRepo.save(existing), Pharmacie.LIBERTE);
    }

    @Transactional(transactionManager = AlmadiesConfig.TM)
    public MedicamentDto modifierAlmadies(Long id, MedicamentDto dto) {
        MedicamentAlmadies existing = almadiesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicament introuvable aux Almadies : " + id));
        MedicamentMapper.copyDtoToEntity(dto, existing);
        return MedicamentMapper.toDto(almadiesRepo.save(existing), Pharmacie.ALMADIES);
    }

    public MedicamentDto modifier(Pharmacie pharmacie, Long id, MedicamentDto dto) {
        return switch (pharmacie) {
            case MEDINA   -> modifierMedina(id, dto);
            case LIBERTE  -> modifierLiberte(id, dto);
            case ALMADIES -> modifierAlmadies(id, dto);
        };
    }

    // =================================================================
    //  Suppression
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM)
    public void supprimerMedina(Long id) {
        if (!medinaRepo.existsById(id)) {
            throw new IllegalArgumentException("Medicament introuvable a Medina : " + id);
        }
        medinaRepo.deleteById(id);
    }

    @Transactional(transactionManager = LiberteConfig.TM)
    public void supprimerLiberte(Long id) {
        if (!liberteRepo.existsById(id)) {
            throw new IllegalArgumentException("Medicament introuvable a Liberte : " + id);
        }
        liberteRepo.deleteById(id);
    }

    @Transactional(transactionManager = AlmadiesConfig.TM)
    public void supprimerAlmadies(Long id) {
        if (!almadiesRepo.existsById(id)) {
            throw new IllegalArgumentException("Medicament introuvable aux Almadies : " + id);
        }
        almadiesRepo.deleteById(id);
    }

    public void supprimer(Pharmacie pharmacie, Long id) {
        switch (pharmacie) {
            case MEDINA   -> supprimerMedina(id);
            case LIBERTE  -> supprimerLiberte(id);
            case ALMADIES -> supprimerAlmadies(id);
        }
    }

    // =================================================================
    //  Recuperation unitaire (pour edition / suppression depuis la vue)
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM, readOnly = true)
    public Optional<MedicamentDto> trouverMedina(Long id) {
        return medinaRepo.findById(id)
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.MEDINA));
    }

    @Transactional(transactionManager = LiberteConfig.TM, readOnly = true)
    public Optional<MedicamentDto> trouverLiberte(Long id) {
        return liberteRepo.findById(id)
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.LIBERTE));
    }

    @Transactional(transactionManager = AlmadiesConfig.TM, readOnly = true)
    public Optional<MedicamentDto> trouverAlmadies(Long id) {
        return almadiesRepo.findById(id)
                .map(m -> MedicamentMapper.toDto(m, Pharmacie.ALMADIES));
    }

    public Optional<MedicamentDto> trouver(Pharmacie pharmacie, Long id) {
        return switch (pharmacie) {
            case MEDINA   -> trouverMedina(id);
            case LIBERTE  -> trouverLiberte(id);
            case ALMADIES -> trouverAlmadies(id);
        };
    }
}
