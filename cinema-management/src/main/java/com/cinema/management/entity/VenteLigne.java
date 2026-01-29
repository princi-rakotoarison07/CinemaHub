package com.cinema.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "vente_ligne")
public class VenteLigne {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "id_vente", nullable = false)
  private Vente vente;

  @ManyToOne(optional = false)
  @JoinColumn(name = "id_produit_extra", nullable = false)
  private ProduitExtra produitExtra;

  @Column(nullable = false)
  private Integer quantite;

  @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
  private BigDecimal prixUnitaire;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Vente getVente() {
    return vente;
  }

  public void setVente(Vente vente) {
    this.vente = vente;
  }

  public ProduitExtra getProduitExtra() {
    return produitExtra;
  }

  public void setProduitExtra(ProduitExtra produitExtra) {
    this.produitExtra = produitExtra;
  }

  public Integer getQuantite() {
    return quantite;
  }

  public void setQuantite(Integer quantite) {
    this.quantite = quantite;
  }

  public BigDecimal getPrixUnitaire() {
    return prixUnitaire;
  }

  public void setPrixUnitaire(BigDecimal prixUnitaire) {
    this.prixUnitaire = prixUnitaire;
  }
}
