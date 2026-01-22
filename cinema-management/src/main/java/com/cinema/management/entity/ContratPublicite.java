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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "contrat_publicite")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContratPublicite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_societe", nullable = false)
  private Societe societe;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_tarif_publicite", nullable = false)
  private TarifPublicite tarifPublicite;

  @Column(name = "nb_diffusions", nullable = false)
  private Integer nbDiffusions;

  @Column(name = "montant_total", nullable = false)
  private BigDecimal montantTotal;

  @Column(name = "date_debut", nullable = false)
  private LocalDate dateDebut;

  @Column(name = "date_fin")
  private LocalDate dateFin;

  @Column(name = "date_contrat", insertable = false, updatable = false)
  private Instant dateContrat;

  private Boolean actif;

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

  public TarifPublicite getTarifPublicite() {
    return tarifPublicite;
  }

  public void setTarifPublicite(TarifPublicite tarifPublicite) {
    this.tarifPublicite = tarifPublicite;
  }

  public Integer getNbDiffusions() {
    return nbDiffusions;
  }

  public void setNbDiffusions(Integer nbDiffusions) {
    this.nbDiffusions = nbDiffusions;
  }

  public BigDecimal getMontantTotal() {
    return montantTotal;
  }

  public void setMontantTotal(BigDecimal montantTotal) {
    this.montantTotal = montantTotal;
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

  public Instant getDateContrat() {
    return dateContrat;
  }

  public void setDateContrat(Instant dateContrat) {
    this.dateContrat = dateContrat;
  }

  public Boolean getActif() {
    return actif;
  }

  public void setActif(Boolean actif) {
    this.actif = actif;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
