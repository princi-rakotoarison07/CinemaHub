package com.cinema.management.controller;

import com.cinema.management.entity.DetailsReservation;
import com.cinema.management.service.DetailsReservationService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/details-reservations")
public class DetailsReservationController {

  private final DetailsReservationService detailsReservationService;

  public DetailsReservationController(DetailsReservationService detailsReservationService) {
    this.detailsReservationService = detailsReservationService;
  }

  @GetMapping
  public List<DetailsReservationDto> getAll(
      @RequestParam(name = "reservationId", required = false) Long reservationId) {
    List<DetailsReservation> list =
        reservationId != null
            ? detailsReservationService.findByReservationId(reservationId)
            : detailsReservationService.findAll();

    return list.stream().map(DetailsReservationController::toDto).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public Optional<DetailsReservationDto> getById(@PathVariable Long id) {
    return detailsReservationService.findById(id).map(DetailsReservationController::toDto);
  }

  private static DetailsReservationDto toDto(DetailsReservation d) {
    Long reservationId = d.getReservation() != null ? d.getReservation().getId() : null;
    Long placeId = d.getPlace() != null ? d.getPlace().getId() : null;
    Long categorieId = d.getCategorieClient() != null ? d.getCategorieClient().getId() : null;

    String rangee = d.getPlace() != null ? d.getPlace().getRangee() : null;
    Integer numero = d.getPlace() != null ? d.getPlace().getNumero() : null;
    String placeLabel =
        (rangee != null && numero != null) ? (rangee + numero) : (placeId != null ? String.valueOf(placeId) : null);

    Long typePlaceId =
        d.getPlace() != null && d.getPlace().getTypePlace() != null ? d.getPlace().getTypePlace().getId() : null;
    String typePlaceLibelle =
        d.getPlace() != null && d.getPlace().getTypePlace() != null ? d.getPlace().getTypePlace().getLibelle() : null;

    String categorieLibelle = d.getCategorieClient() != null ? d.getCategorieClient().getLibelle() : null;

    return new DetailsReservationDto(
        d.getId(),
        reservationId != null ? new ReservationRef(reservationId) : null,
        placeId != null ? new PlaceRef(placeId, rangee, numero, placeLabel, typePlaceId, typePlaceLibelle) : null,
        categorieId != null ? new CategorieClientRef(categorieId, categorieLibelle) : null);
  }

  public record ReservationRef(Long id) {}

  public record PlaceRef(Long id, String rangee, Integer numero, String label, Long typePlaceId, String typePlaceLibelle) {}

  public record CategorieClientRef(Long id, String libelle) {}

  public record DetailsReservationDto(
      Long id,
      ReservationRef reservation,
      PlaceRef place,
      CategorieClientRef categorieClient) {}
}
