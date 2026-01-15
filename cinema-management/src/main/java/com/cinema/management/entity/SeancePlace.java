package com.cinema.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "seance_place")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SeancePlace {

  @EmbeddedId
  private SeancePlaceId id;

  @MapsId("seanceId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_seance", nullable = false)
  private Seance seance;

  @MapsId("placeId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_place", nullable = false)
  private Place place;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_type_place", nullable = false)
  private TypePlace typePlace;

  public SeancePlaceId getId() {
    return id;
  }

  public void setId(SeancePlaceId id) {
    this.id = id;
  }

  public Seance getSeance() {
    return seance;
  }

  public void setSeance(Seance seance) {
    this.seance = seance;
  }

  public Place getPlace() {
    return place;
  }

  public void setPlace(Place place) {
    this.place = place;
  }

  public TypePlace getTypePlace() {
    return typePlace;
  }

  public void setTypePlace(TypePlace typePlace) {
    this.typePlace = typePlace;
  }
}
