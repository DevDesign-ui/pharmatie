package com.pharmasante.pharmadb.service;

import com.pharmasante.pharmadb.config.AlmadiesConfig;
import com.pharmasante.pharmadb.config.LiberteConfig;
import com.pharmasante.pharmadb.config.MedinaConfig;
import com.pharmasante.pharmadb.entity.almadies.MedicamentAlmadies;
import com.pharmasante.pharmadb.entity.liberte.MedicamentLiberte;
import com.pharmasante.pharmadb.entity.medina.MedicamentMedina;
import com.pharmasante.pharmadb.mapper.MedicamentMapper;
import com.pharmasante.pharmadb.model.MedicamentDto;
import com.pharmasante.pharmadb.model.Pharmacie;
import com.pharmasante.pharmadb.repository.almadies.MedicamentAlmadiesRepository;
import com.pharmasante.pharmadb.repository.liberte.MedicamentLiberteRepository;
import com.pharmasante.pharmadb.repository.medina.MedicamentMedinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service de synchronisation automatique entre les trois bases de pharmacies.
 *
 * Principe :
 *  1. Charger l'ensemble des medicaments des trois pharmacies.
 *  2. Pour chaque code de medicament connu, determiner la version la plus
 *     recente (champ {@code updatedAt}) : c'est la version de reference.
 *  3. Pour chaque pharmacie :
 *     - si le medicament n'existe pas -> le creer (copie depuis la reference) ;
 *     - s'il existe mais qu'il est plus ancien -> le mettre a jour ;
 *     - s'il est deja a jour -> ne rien faire.
 *
 * La planification est declenchee toutes les minutes via {@code @Scheduled}.
 * Le rate est configurable via la propriete {@code pharmasante.sync.fixed-rate-ms}
 * et la synchronisation peut etre desactivee via {@code pharmasante.sync.enabled}.
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final MedicamentMedinaRepository   medinaRepo;
    private final MedicamentLiberteRepository  liberteRepo;
    private final MedicamentAlmadiesRepository almadiesRepo;

    @Value("${pharmasante.sync.enabled:true}")
    private boolean syncActivee;

    public SyncService(MedicamentMedinaRepository medinaRepo,
                       MedicamentLiberteRepository liberteRepo,
                       MedicamentAlmadiesRepository almadiesRepo) {
        this.medinaRepo   = medinaRepo;
        this.liberteRepo  = liberteRepo;
        this.almadiesRepo = almadiesRepo;
    }

    // =================================================================
    //  Planification
    // =================================================================

    @Scheduled(fixedRateString = "${pharmasante.sync.fixed-rate-ms:60000}")
    public void synchroniserAutomatiquement() {
        if (!syncActivee) {
            return;
        }
        log.info("=== Demarrage de la synchronisation automatique ===");
        SyncRapport rapport = synchroniser();
        log.info("=== Synchronisation terminee : {} copie(s), {} mise(s) a jour, {} conflit(s) resolu(s) ===",
                rapport.copies, rapport.misesAJour, rapport.conflits);
    }

    // =================================================================
    //  Coeur de la synchronisation
    // =================================================================

    /**
     * Execute une synchronisation complete et renvoie un rapport detaille.
     * Appele aussi bien par la planification que par le controleur (via
     * {@link MultiStockService} ou directement) pour declencher une synchro
     * manuelle depuis l'interface web.
     */
    public SyncRapport synchroniser() {
        SyncRapport rapport = new SyncRapport();

        // 1) Chargement de toutes les donnees.
        Map<String, MedicamentDto> refMedina   = chargerMedina();
        Map<String, MedicamentDto> refLiberte  = chargerLiberte();
        Map<String, MedicamentDto> refAlmadies = chargerAlmadies();

        // 2) Determination de la version la plus recente pour chaque code.
        Map<String, MedicamentDto> versionDeReference = new HashMap<>();
        mettreAJourReference(versionDeReference, refMedina,   rapport);
        mettreAJourReference(versionDeReference, refLiberte,  rapport);
        mettreAJourReference(versionDeReference, refAlmadies, rapport);

        // 3) Replication de la version de reference dans chaque base.
        for (Map.Entry<String, MedicamentDto> entry : versionDeReference.entrySet()) {
            MedicamentDto reference = entry.getValue();

            appliquerVersMedina(reference, rapport);
            appliquerVersLiberte(reference, rapport);
            appliquerVersAlmadies(reference, rapport);
        }

        return rapport;
    }

    /**
     * Conserve, pour chaque code, la version la plus recente.
     * Un conflit est detecte lorsqu'un meme code possede des {@code updatedAt}
     * differents entre deux bases : la version la plus recente gagne et le
     * compteur de conflits est incremente.
     */
    private void mettreAJourReference(Map<String, MedicamentDto> reference,
                                      Map<String, MedicamentDto> source,
                                      SyncRapport rapport) {
        for (Map.Entry<String, MedicamentDto> e : source.entrySet()) {
            String code = e.getKey();
            MedicamentDto candidate = e.getValue();
            MedicamentDto courante = reference.get(code);
            if (courante == null) {
                reference.put(code, candidate);
            } else {
                LocalDateTime cdt = candidate.getUpdatedAt() == null ? LocalDateTime.MIN : candidate.getUpdatedAt();
                LocalDateTime cur = courante.getUpdatedAt() == null ? LocalDateTime.MIN : courante.getUpdatedAt();
                if (!Objects.equals(cdt, cur)) {
                    rapport.conflits++;
                }
                if (cdt.isAfter(cur)) {
                    reference.put(code, candidate);
                }
            }
        }
    }

    // =================================================================
    //  Application vers chaque base
    // =================================================================

    private void appliquerVersMedina(MedicamentDto reference, SyncRapport rapport) {
        Optional<MedicamentMedina> existant = medinaRepo.findByCode(reference.getCode());
        if (existant.isEmpty()) {
            MedicamentMedina e = MedicamentMapper.newMedinaEntity(reference);
            medinaRepo.save(e);
            rapport.copies++;
        } else {
            MedicamentMedina cible = existant.get();
            if (doitEtreMisAJour(cible.getUpdatedAt(), reference.getUpdatedAt())) {
                MedicamentMapper.copyDtoToEntity(reference, cible);
                medinaRepo.save(cible);
                rapport.misesAJour++;
            }
        }
    }

    private void appliquerVersLiberte(MedicamentDto reference, SyncRapport rapport) {
        Optional<MedicamentLiberte> existant = liberteRepo.findByCode(reference.getCode());
        if (existant.isEmpty()) {
            MedicamentLiberte e = MedicamentMapper.newLiberteEntity(reference);
            liberteRepo.save(e);
            rapport.copies++;
        } else {
            MedicamentLiberte cible = existant.get();
            if (doitEtreMisAJour(cible.getUpdatedAt(), reference.getUpdatedAt())) {
                MedicamentMapper.copyDtoToEntity(reference, cible);
                liberteRepo.save(cible);
                rapport.misesAJour++;
            }
        }
    }

    private void appliquerVersAlmadies(MedicamentDto reference, SyncRapport rapport) {
        Optional<MedicamentAlmadies> existant = almadiesRepo.findByCode(reference.getCode());
        if (existant.isEmpty()) {
            MedicamentAlmadies e = MedicamentMapper.newAlmadiesEntity(reference);
            almadiesRepo.save(e);
            rapport.copies++;
        } else {
            MedicamentAlmadies cible = existant.get();
            if (doitEtreMisAJour(cible.getUpdatedAt(), reference.getUpdatedAt())) {
                MedicamentMapper.copyDtoToEntity(reference, cible);
                almadiesRepo.save(cible);
                rapport.misesAJour++;
            }
        }
    }

    /**
     * Decide si l'entite cible doit etre mise a jour : oui uniquement si la
     * version de reference est strictement plus recente.
     */
    private boolean doitEtreMisAJour(LocalDateTime cibleUpdatedAt, LocalDateTime referenceUpdatedAt) {
        LocalDateTime c = cibleUpdatedAt == null ? LocalDateTime.MIN : cibleUpdatedAt;
        LocalDateTime r = referenceUpdatedAt == null ? LocalDateTime.MIN : referenceUpdatedAt;
        return r.isAfter(c);
    }

    // =================================================================
    //  Chargement initial des donnees de chaque base
    // =================================================================

    @Transactional(transactionManager = MedinaConfig.TM, readOnly = true)
    protected Map<String, MedicamentDto> chargerMedina() {
        Map<String, MedicamentDto> map = new HashMap<>();
        for (MedicamentMedina m : medinaRepo.findAll()) {
            map.put(m.getCode(), MedicamentMapper.toDto(m, Pharmacie.MEDINA));
        }
        return map;
    }

    @Transactional(transactionManager = LiberteConfig.TM, readOnly = true)
    protected Map<String, MedicamentDto> chargerLiberte() {
        Map<String, MedicamentDto> map = new HashMap<>();
        for (MedicamentLiberte m : liberteRepo.findAll()) {
            map.put(m.getCode(), MedicamentMapper.toDto(m, Pharmacie.LIBERTE));
        }
        return map;
    }

    @Transactional(transactionManager = AlmadiesConfig.TM, readOnly = true)
    protected Map<String, MedicamentDto> chargerAlmadies() {
        Map<String, MedicamentDto> map = new HashMap<>();
        for (MedicamentAlmadies m : almadiesRepo.findAll()) {
            map.put(m.getCode(), MedicamentMapper.toDto(m, Pharmacie.ALMADIES));
        }
        return map;
    }

    // =================================================================
    //  Rapport de synchronisation
    // =================================================================

    /** Compte-rendu d'une synchronisation. */
    public static class SyncRapport {
        private int copies;
        private int misesAJour;
        private int conflits;
        private final LocalDateTime date = LocalDateTime.now();

        public int getCopies() { return copies; }
        public int getMisesAJour() { return misesAJour; }
        public int getConflits() { return conflits; }
        public LocalDateTime getDate() { return date; }

        @Override
        public String toString() {
            return String.format("copies=%d, misesAJour=%d, conflits=%d", copies, misesAJour, conflits);
        }
    }
}
