package com.pharmasante.pharmadb.web;

import com.pharmasante.pharmadb.model.MedicamentDto;
import com.pharmasante.pharmadb.model.Pharmacie;
import com.pharmasante.pharmadb.service.MultiStockService;
import com.pharmasante.pharmadb.service.SyncService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Controleur Web exposes via Thymeleaf. Couvre toutes les fonctionnalités
 * demandées :
 *  - tableau de bord ;
 *  - stock d'une pharmacie ;
 *  - vue consolidée ;
 *  - formulaire d'ajout / modification ;
 *  - suppression ;
 *  - recherche par code ;
 *  - synchronisation manuelle (en plus de l'automatique).
 */
@Controller
@RequestMapping("/")
public class PharmaController {

    private final MultiStockService stockService;
    private final SyncService syncService;

    public PharmaController(MultiStockService stockService, SyncService syncService) {
        this.stockService = stockService;
        this.syncService = syncService;
    }

    // =================================================================
    //  Tableau de bord
    // =================================================================

    @GetMapping
    public String dashboard(Model model) {
        List<MedicamentDto> medina   = stockService.listerMedina();
        List<MedicamentDto> liberte  = stockService.listerLiberte();
        List<MedicamentDto> almadies = stockService.listerAlmadies();

        model.addAttribute("medina",   medina);
        model.addAttribute("liberte",  liberte);
        model.addAttribute("almadies", almadies);
        model.addAttribute("totalMedina",   medina.size());
        model.addAttribute("totalLiberte",  liberte.size());
        model.addAttribute("totalAlmadies", almadies.size());
        model.addAttribute("totalGlobal",   medina.size() + liberte.size() + almadies.size());

        return "dashboard";
    }

    // =================================================================
    //  Stock d'une pharmacie
    // =================================================================

    @GetMapping("/pharmacie/{nom}")
    public String stockPharmacie(@PathVariable("nom") String nom, Model model, RedirectAttributes ra) {
        Pharmacie pharmacie = parsePharmacie(nom, ra);
        if (pharmacie == null) {
            return "redirect:/";
        }
        model.addAttribute("pharmacie", pharmacie);
        model.addAttribute("medicaments", stockService.listerParPharmacie(pharmacie));
        return "pharmacie";
    }

    // =================================================================
    //  Vue consolidée
    // =================================================================

    @GetMapping("/consolide")
    public String stockConsolide(Model model) {
        List<MedicamentDto> tous = stockService.stockConsolide();
        model.addAttribute("medicaments", tous);
        model.addAttribute("total", tous.size());
        return "consolide";
    }

    // =================================================================
    //  Recherche par code
    // =================================================================

    @GetMapping("/recherche")
    public String recherche(@RequestParam(value = "code", required = false) String code, Model model) {
        if (code != null && !code.isBlank()) {
            model.addAttribute("code", code);
            model.addAttribute("resultats", stockService.rechercherPartout(code));
        }
        return "recherche";
    }

    // =================================================================
    //  Ajout
    // =================================================================

    @GetMapping("/medicament/nouveau")
    public String formNouveau(@RequestParam(value = "pharmacie", required = false) String pharmacie,
                              Model model) {
        MedicamentDto dto = new MedicamentDto();
        if (pharmacie != null && !pharmacie.isBlank()) {
            dto.setPharmacie(parsePharmacieSilent(pharmacie));
        }
        model.addAttribute("medicament", dto);
        model.addAttribute("pharmacies", Arrays.asList(Pharmacie.values()));
        return "form";
    }

    @PostMapping("/medicament")
    public String enregistrer(@ModelAttribute("medicament") MedicamentForm form,
                              RedirectAttributes ra) {
        try {
            Pharmacie pharmacie = Pharmacie.valueOf(form.getPharmacie().toUpperCase());
            MedicamentDto dto = com.pharmasante.pharmadb.mapper.MedicamentMapper.buildDto(
                    form.getCode(), form.getNom(), form.getDescription(), form.getCategorie(),
                    form.getPrix(), form.getQuantite(),
                    form.getDateFabrication(), form.getDateExpiration(),
                    pharmacie);

            if (form.getId() != null && form.getId() > 0) {
                stockService.modifier(pharmacie, form.getId(), dto);
                ra.addFlashAttribute("succes", "Medicament modifie avec succes.");
            } else {
                stockService.ajouter(dto);
                ra.addFlashAttribute("succes", "Medicament ajoute avec succes.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/pharmacie/" + form.getPharmacie().toLowerCase();
    }

    // =================================================================
    //  Modification
    // =================================================================

    @GetMapping("/medicament/{pharmacie}/{id}/modifier")
    public String formModifier(@PathVariable("pharmacie") String nomPharmacie,
                               @PathVariable("id") Long id,
                               Model model,
                               RedirectAttributes ra) {
        Pharmacie pharmacie = parsePharmacie(nomPharmacie, ra);
        if (pharmacie == null) {
            return "redirect:/";
        }
        MedicamentDto dto = stockService.trouver(pharmacie, id)
                .orElse(null);
        if (dto == null) {
            ra.addFlashAttribute("erreur", "Medicament introuvable.");
            return "redirect:/pharmacie/" + nomPharmacie;
        }
        model.addAttribute("medicament", dto);
        model.addAttribute("pharmacies", Arrays.asList(Pharmacie.values()));
        return "form";
    }

    // =================================================================
    //  Suppression
    // =================================================================

    @PostMapping("/medicament/{pharmacie}/{id}/supprimer")
    public String supprimer(@PathVariable("pharmacie") String nomPharmacie,
                            @PathVariable("id") Long id,
                            RedirectAttributes ra) {
        Pharmacie pharmacie = parsePharmacie(nomPharmacie, ra);
        if (pharmacie == null) {
            return "redirect:/";
        }
        try {
            stockService.supprimer(pharmacie, id);
            ra.addFlashAttribute("succes", "Medicament supprime avec succes.");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/pharmacie/" + nomPharmacie;
    }

    // =================================================================
    //  Synchronisation manuelle
    // =================================================================

    @PostMapping("/sync")
    public String synchroniserManuellement(RedirectAttributes ra) {
        SyncService.SyncRapport rapport = syncService.synchroniser();
        ra.addFlashAttribute("succes",
                "Synchronisation effectuee : " + rapport.getCopies() + " copie(s), "
                        + rapport.getMisesAJour() + " mise(s) a jour, "
                        + rapport.getConflits() + " conflit(s) resolu(s).");
        return "redirect:/consolide";
    }

    // =================================================================
    //  Utilitaires
    // =================================================================

    private Pharmacie parsePharmacie(String nom, RedirectAttributes ra) {
        try {
            return Pharmacie.valueOf(nom.toUpperCase());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erreur", "Pharmacie inconnue : " + nom);
            return null;
        }
    }

    private Pharmacie parsePharmacieSilent(String nom) {
        try {
            return Pharmacie.valueOf(nom.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Formulaire de saisie. Les champs prix / quantite sont degrades en
     * String cote Thymeleaf puis parses ici pour garder un message d'erreur
     * explicite en cas de saisie invalide.
     */
    public static class MedicamentForm {
        private Long id;
        private String pharmacie;
        private String code;
        private String nom;
        private String description;
        private String categorie;
        private BigDecimal prix;
        private Integer quantite;
        private LocalDate dateFabrication;
        private LocalDate dateExpiration;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPharmacie() { return pharmacie; }
        public void setPharmacie(String pharmacie) { this.pharmacie = pharmacie; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategorie() { return categorie; }
        public void setCategorie(String categorie) { this.categorie = categorie; }
        public BigDecimal getPrix() { return prix; }
        public void setPrix(BigDecimal prix) { this.prix = prix; }
        public Integer getQuantite() { return quantite; }
        public void setQuantite(Integer quantite) { this.quantite = quantite; }
        public LocalDate getDateFabrication() { return dateFabrication; }
        public void setDateFabrication(LocalDate dateFabrication) { this.dateFabrication = dateFabrication; }
        public LocalDate getDateExpiration() { return dateExpiration; }
        public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    }
}
