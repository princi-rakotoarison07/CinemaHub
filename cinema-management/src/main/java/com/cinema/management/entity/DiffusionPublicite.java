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
@Table(name = "diffusion_publicite")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DiffusionPublicite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_contrat_publicite", nullable = false)
  private ContratPublicite contratPublicite;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_seance", nullable = false)
  private Seance seance;

  @Column(name = "nombre_pub", nullable = false)
  private Integer nombrePub;

  @Column(name = "cree_le", insertable = false, updatable = false)
  private Instant creeLe;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ContratPublicite getContratPublicite() {
    return contratPublicite;
  }

  public void setContratPublicite(ContratPublicite contratPublicite) {
    this.contratPublicite = contratPublicite;
  }

  public Seance getSeance() {
    return seance;
  }

  public void setSeance(Seance seance) {
    this.seance = seance;
  }

  public Integer getNombrePub() {
    return nombrePub;
  }

  public void setNombrePub(Integer nombrePub) {
    this.nombrePub = nombrePub;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
