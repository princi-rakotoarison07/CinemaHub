package com.cinema.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SeancePlaceId implements Serializable {

  @Column(name = "id_seance", nullable = false)
  private Long seanceId;

  @Column(name = "id_place", nullable = false)
  private Long placeId;

  public SeancePlaceId() {}

  public SeancePlaceId(Long seanceId, Long placeId) {
    this.seanceId = seanceId;
    this.placeId = placeId;
  }

  public Long getSeanceId() {
    return seanceId;
  }

  public void setSeanceId(Long seanceId) {
    this.seanceId = seanceId;
  }

  public Long getPlaceId() {
    return placeId;
  }

  public void setPlaceId(Long placeId) {
    this.placeId = placeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SeancePlaceId that = (SeancePlaceId) o;
    return Objects.equals(seanceId, that.seanceId) && Objects.equals(placeId, that.placeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(seanceId, placeId);
  }
}
