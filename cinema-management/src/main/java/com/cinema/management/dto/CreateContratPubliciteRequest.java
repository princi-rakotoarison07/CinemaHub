package com.cinema.management.dto;

import java.time.LocalDate;
import java.util.List;

public class CreateContratPubliciteRequest {

  private Long videoPublicitaireId;
  private Integer nbDiffusions;
  private LocalDate dateDebut;
  private LocalDate dateFin;
  private List<DiffusionSeance> diffusions;

  public Long getVideoPublicitaireId() {
    return videoPublicitaireId;
  }

  public void setVideoPublicitaireId(Long videoPublicitaireId) {
    this.videoPublicitaireId = videoPublicitaireId;
  }

  public Integer getNbDiffusions() {
    return nbDiffusions;
  }

  public void setNbDiffusions(Integer nbDiffusions) {
    this.nbDiffusions = nbDiffusions;
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

  public List<DiffusionSeance> getDiffusions() {
    return diffusions;
  }

  public void setDiffusions(List<DiffusionSeance> diffusions) {
    this.diffusions = diffusions;
  }

  public static class DiffusionSeance {
    private Long seanceId;
    private Integer nombrePub;

    public Long getSeanceId() {
      return seanceId;
    }

    public void setSeanceId(Long seanceId) {
      this.seanceId = seanceId;
    }

    public Integer getNombrePub() {
      return nombrePub;
    }

    public void setNombrePub(Integer nombrePub) {
      this.nombrePub = nombrePub;
    }
  }
}
