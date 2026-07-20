package com.pharmasante.pharmadb.mapper;

import com.pharmasante.pharmadb.entity.AbstractMedicament;
import com.pharmasante.pharmadb.entity.almadies.MedicamentAlmadies;
import com.pharmasante.pharmadb.entity.liberte.MedicamentLiberte;
import com.pharmasante.pharmadb.entity.medina.MedicamentMedina;
import com.pharmasante.pharmadb.model.MedicamentDto;
import com.pharmasante.pharmadb.model.Pharmacie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Utilitaire de conversion entre les entites JPA propres a chaque pharmacie
 * et le DTO neutre {@link MedicamentDto} utilise partout ailleurs
 * (consolidation, synchronisation, vues Thymeleaf).
 */
public final class MedicamentMapper {

    private MedicamentMapper() {
    }

    public static MedicamentDto toDto(AbstractMedicament m, Pharmacie pharmacie) {
        if (m == null) {
            return null;
        }
        MedicamentDto dto = new MedicamentDto();
        dto.setId(m.getId());
        dto.setCode(m.getCode());
        dto.setNom(m.getNom());
        dto.setDescription(m.getDescription());
        dto.setCategorie(m.getCategorie());
        dto.setPrix(m.getPrix());
        dto.setQuantite(m.getQuantite());
        dto.setDateFabrication(m.getDateFabrication());
        dto.setDateExpiration(m.getDateExpiration());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setUpdatedAt(m.getUpdatedAt());
        dto.setPharmacie(pharmacie);
        return dto;
    }

    public static void copyDtoToEntity(MedicamentDto src, AbstractMedicament dest) {
        dest.setCode(src.getCode());
        dest.setNom(src.getNom());
        dest.setDescription(src.getDescription());
        dest.setCategorie(src.getCategorie());
        dest.setPrix(src.getPrix());
        dest.setQuantite(src.getQuantite());
        dest.setDateFabrication(src.getDateFabrication());
        dest.setDateExpiration(src.getDateExpiration());
        if (src.getUpdatedAt() != null) {
            dest.setUpdatedAt(src.getUpdatedAt());
        }
    }

    public static MedicamentMedina newMedinaEntity(MedicamentDto dto) {
        MedicamentMedina e = new MedicamentMedina();
        copyDtoToEntity(dto, e);
        return e;
    }

    public static MedicamentLiberte newLiberteEntity(MedicamentDto dto) {
        MedicamentLiberte e = new MedicamentLiberte();
        copyDtoToEntity(dto, e);
        return e;
    }

    public static MedicamentAlmadies newAlmadiesEntity(MedicamentDto dto) {
        MedicamentAlmadies e = new MedicamentAlmadies();
        copyDtoToEntity(dto, e);
        return e;
    }

    public static AbstractMedicament newEntityFor(Pharmacie p, MedicamentDto dto) {
        return switch (p) {
            case MEDINA   -> newMedinaEntity(dto);
            case LIBERTE  -> newLiberteEntity(dto);
            case ALMADIES -> newAlmadiesEntity(dto);
        };
    }

    /** Reconstruit un DTO coherent a partir des champs saisis dans le formulaire. */
    public static MedicamentDto buildDto(String code, String nom, String description, String categorie,
                                         BigDecimal prix, Integer quantite,
                                         LocalDate fabrication, LocalDate expiration,
                                         Pharmacie pharmacie) {
        MedicamentDto dto = new MedicamentDto();
        dto.setCode(code);
        dto.setNom(nom);
        dto.setDescription(description);
        dto.setCategorie(categorie);
        dto.setPrix(prix);
        dto.setQuantite(quantite);
        dto.setDateFabrication(fabrication);
        dto.setDateExpiration(expiration);
        dto.setPharmacie(pharmacie);
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}
