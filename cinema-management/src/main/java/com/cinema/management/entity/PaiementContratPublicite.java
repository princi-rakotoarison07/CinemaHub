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

@Entity
@Table(name = "paiement_contrat_publicite")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaiementContratPublicite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_contrat_publicite", nullable = false)
  private ContratPublicite contratPublicite;

  @Column(name = "date_paiement")
  private Instant datePaiement;

  @Column(nullable = false)
  private BigDecimal montant;

  private String mode;

  private String reference;

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

  public Instant getDatePaiement() {
    return datePaiement;
  }

  public void setDatePaiement(Instant datePaiement) {
    this.datePaiement = datePaiement;
  }

  public BigDecimal getMontant() {
    return montant;
  }

  public void setMontant(BigDecimal montant) {
    this.montant = montant;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Instant getCreeLe() {
    return creeLe;
  }

  public void setCreeLe(Instant creeLe) {
    this.creeLe = creeLe;
  }
}
