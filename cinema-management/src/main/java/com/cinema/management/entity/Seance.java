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
import java.time.Instant;

@Entity
@Table(name = "seance")
public class Seance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_film", nullable = false)
  private Film film;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_salle", nullable = false)
  private Salle salle;

  @Column(name = "date_heure", nullable = false)
  private Instant dateHeure;

  private String langue;

  private String version;

  @Column(name = "cree_le", insertable = false, updatable = false)
  private Instant creeLe;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Film getFilm() {
    return film;
  }

  public void setFilm(Film film) {
    this.film = film;
  }

  public Salle getSalle() {
    return salle;
  }

  public void setSalle(Salle salle) {
    this.salle = salle;
  }

  public Instant getDateHeure() {
    return dateHeure;
  }

  public void setDateHeure(Instant dateHeure) {
    this.dateHeure = dateHeure;
  }

  public String getLangue() {
    return langue;
  }

  public void setLangue(String langue) {
    this.langue = langue;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
