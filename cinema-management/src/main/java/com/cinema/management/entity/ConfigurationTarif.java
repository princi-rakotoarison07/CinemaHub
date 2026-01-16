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

@Entity
@Table(name = "configuration_tarif")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConfigurationTarif {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_tarif1", nullable = false)
  private Tarif tarif1;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_tarif2", nullable = false)
  private Tarif tarif2;

  @Column(nullable = false)
  private BigDecimal pourcentage;

  private Boolean actif;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Tarif getTarif1() {
    return tarif1;
  }

  public void setTarif1(Tarif tarif1) {
    this.tarif1 = tarif1;
  }

  public Tarif getTarif2() {
    return tarif2;
  }

  public void setTarif2(Tarif tarif2) {
    this.tarif2 = tarif2;
  }

  public BigDecimal getPourcentage() {
    return pourcentage;
  }

  public void setPourcentage(BigDecimal pourcentage) {
    this.pourcentage = pourcentage;
  }

  public Boolean getActif() {
    return actif;
  }

  public void setActif(Boolean actif) {
    this.actif = actif;
  }
}
