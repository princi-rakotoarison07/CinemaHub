package com.cinema.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "film")
public class Film {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String titre;

  private String description;

  @Column(name = "duree_minutes", nullable = false)
  private Integer dureeMinutes;

  @Column(name = "date_sortie")
  private LocalDate dateSortie;

  @Column(name = "age_min")
  private Integer ageMin;

  @Column(name = "langue_originale")
  private String langueOriginale;

  @Column(name = "cree_le", insertable = false, updatable = false)
  private Instant creeLe;

  @ManyToMany
  @JoinTable(
      name = "film_genre",
      joinColumns = @JoinColumn(name = "id_film"),
      inverseJoinColumns = @JoinColumn(name = "id_genre"))
  private Set<Genre> genres = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitre() {
    return titre;
  }

  public void setTitre(String titre) {
    this.titre = titre;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getDureeMinutes() {
    return dureeMinutes;
  }

  public void setDureeMinutes(Integer dureeMinutes) {
    this.dureeMinutes = dureeMinutes;
  }

  public LocalDate getDateSortie() {
    return dateSortie;
  }

  public void setDateSortie(LocalDate dateSortie) {
    this.dateSortie = dateSortie;
  }

  public Integer getAgeMin() {
    return ageMin;
  }

  public void setAgeMin(Integer ageMin) {
    this.ageMin = ageMin;
  }

  public String getLangueOriginale() {
    return langueOriginale;
  }

  public void setLangueOriginale(String langueOriginale) {
    this.langueOriginale = langueOriginale;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }

  public Set<Genre> getGenres() {
    return genres;
  }

  public void setGenres(Set<Genre> genres) {
    this.genres = genres;
  }
}
