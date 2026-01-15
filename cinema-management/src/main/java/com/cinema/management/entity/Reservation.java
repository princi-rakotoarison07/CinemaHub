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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "reservation")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_client", nullable = false)
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_seance", nullable = false)
  private Seance seance;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_statut", nullable = false)
  private Statut statut;

  @Column(name = "nb_place", nullable = false)
  private Integer nbPlace;

  @Column(name = "montant_total", nullable = false)
  private BigDecimal montantTotal;

  @Column(name = "date_reservation", insertable = false, updatable = false)
  private Instant dateReservation;

  @Column(name = "date_expiration")
  private Instant dateExpiration;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Seance getSeance() {
    return seance;
  }

  public void setSeance(Seance seance) {
    this.seance = seance;
  }

  public Statut getStatut() {
    return statut;
  }

  public void setStatut(Statut statut) {
    this.statut = statut;
  }

  public Integer getNbPlace() {
    return nbPlace;
  }

  public void setNbPlace(Integer nbPlace) {
    this.nbPlace = nbPlace;
  }

  public BigDecimal getMontantTotal() {
    return montantTotal;
  }

  public void setMontantTotal(BigDecimal montantTotal) {
    this.montantTotal = montantTotal;
  }

  public Instant getDateReservation() {
    return dateReservation;
  }

  public void setDateReservation(Instant dateReservation) {
    this.dateReservation = dateReservation;
  }

  public Instant getDateExpiration() {
    return dateExpiration;
  }

  public void setDateExpiration(Instant dateExpiration) {
    this.dateExpiration = dateExpiration;
  }
}
