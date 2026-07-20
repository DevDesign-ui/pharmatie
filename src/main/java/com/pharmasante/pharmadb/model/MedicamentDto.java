package com.pharmasante.pharmadb.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO neutre representant un medicament issu de n'importe quelle pharmacie.
 *
 * Il sert de structure d'echange unifiee pour :
 *  - la vue consolidee (fusion des stocks des trois bases) ;
 *  - la synchronisation (comparaison et copie entre bases) ;
 *  - l'affichage dans les templates Thymeleaf.
 *
 * Contrairement aux entites JPA qui sont liees a un persistence unit precis,
 * ce DTO est independant de toute source de donnees.
 */
public class MedicamentDto {

    private Long id;
    private String code;
    private String nom;
    private String description;
    private String categorie;
    private BigDecimal prix;
    private Integer quantite;
    private LocalDate dateFabrication;
    private LocalDate dateExpiration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Pharmacie pharmacie;

    public MedicamentDto() {
    }

    public MedicamentDto(String code, String nom, BigDecimal prix, Integer quantite,
                         Pharmacie pharmacie) {
        this.code = code;
        this.nom = nom;
        this.prix = prix;
        this.quantite = quantite;
        this.pharmacie = pharmacie;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Pharmacie getPharmacie() { return pharmacie; }
    public void setPharmacie(Pharmacie pharmacie) { this.pharmacie = pharmacie; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicamentDto that)) return false;
        return Objects.equals(code, that.code)
                && Objects.equals(pharmacie, that.pharmacie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, pharmacie);
    }
}
