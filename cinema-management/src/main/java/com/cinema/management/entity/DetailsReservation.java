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

@Entity
@Table(name = "details_reservation")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DetailsReservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_reservation", nullable = false)
  private Reservation reservation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_place", nullable = false)
  private Place place;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_categorie_client", nullable = false)
  private CategorieClient categorieClient;

  @Column(name = "cree_le", insertable = false, updatable = false)
  private Instant creeLe;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Reservation getReservation() {
    return reservation;
  }

  public void setReservation(Reservation reservation) {
    this.reservation = reservation;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
  }

  public CategorieClient getCategorieClient() {
    return categorieClient;
  }

  public void setCategorieClient(CategorieClient categorieClient) {
    this.categorieClient = categorieClient;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
