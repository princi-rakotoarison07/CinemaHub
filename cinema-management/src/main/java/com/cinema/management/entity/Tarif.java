package com.cinema.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tarif")
public class Tarif {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_type_place", nullable = false)
  private TypePlace typePlace;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_categorie_client", nullable = false)
  private CategorieClient categorieClient;

  @Column(nullable = false)
  private BigDecimal prix;

  private Boolean actif;

  @Column(name = "date_debut")
  private LocalDate dateDebut;

  @Column(name = "date_fin")
  private LocalDate dateFin;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TypePlace getTypePlace() {
    return typePlace;
  }

  public void setTypePlace(TypePlace typePlace) {
    this.typePlace = typePlace;
  }

  public CategorieClient getCategorieClient() {
    return categorieClient;
  }

  public void setCategorieClient(CategorieClient categorieClient) {
    this.categorieClient = categorieClient;
  }

  public BigDecimal getPrix() {
    return prix;
  }

  public void setPrix(BigDecimal prix) {
    this.prix = prix;
  }

  public Boolean getActif() {
    return actif;
  }

  public void setActif(Boolean actif) {
    this.actif = actif;
  }

  public LocalDate getDateDebut() {
    return dateDebut;
  }

  public void setDateDebut(LocalDate dateDebut) {
    this.dateDebut = dateDebut;
  }

  public LocalDate getDateFin() {
    return dateFin;
  }

  public void setDateFin(LocalDate dateFin) {
    this.dateFin = dateFin;
  }
}
