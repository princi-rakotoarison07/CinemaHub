package com.cinema.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "video_publicitaire")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VideoPublicitaire {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_societe", nullable = false)
  private Societe societe;

  @Column(nullable = false)
  private String titre;

  @Column(name = "duree_secondes", nullable = false)
  private Integer dureeSecondes;

  @Column(name = "date_creation")
  private LocalDate dateCreation;

  @Column(name = "cree_le", insertable = false, updatable = false)
  private Instant creeLe;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Societe getSociete() {
    return societe;
  }

  public void setSociete(Societe societe) {
    this.societe = societe;
  }

  public String getTitre() {
    return titre;
  }

  public void setTitre(String titre) {
    this.titre = titre;
  }

  public Integer getDureeSecondes() {
    return dureeSecondes;
  }

  public void setDureeSecondes(Integer dureeSecondes) {
    this.dureeSecondes = dureeSecondes;
  }

  public LocalDate getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(LocalDate dateCreation) {
    this.dateCreation = dateCreation;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
