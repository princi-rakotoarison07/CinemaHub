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
@Table(name = "place")
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_salle", nullable = false)
  private Salle salle;

  @Column(nullable = false)
  private String rangee;

  @Column(nullable = false)
  private Integer numero;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_type_place", nullable = false)
  private TypePlace typePlace;

  @Column(name = "cree_le", insertable = false, updatable = false)
  private Instant creeLe;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Salle getSalle() {
    return salle;
  }

  public void setSalle(Salle salle) {
    this.salle = salle;
  }

  public String getRangee() {
    return rangee;
  }

  public void setRangee(String rangee) {
    this.rangee = rangee;
  }

  public Integer getNumero() {
    return numero;
  }

  public void setNumero(Integer numero) {
    this.numero = numero;
  }

  public TypePlace getTypePlace() {
    return typePlace;
  }

  public void setTypePlace(TypePlace typePlace) {
    this.typePlace = typePlace;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
