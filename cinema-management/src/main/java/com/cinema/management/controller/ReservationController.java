package com.cinema.management.controller;

import com.cinema.management.entity.Reservation;
import com.cinema.management.service.ReservationService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

  private final ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @GetMapping
  public List<ReservationDto> getAll() {
    return reservationService.findAll().stream()
        .map(ReservationController::toDto)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public Optional<ReservationDto> getById(@PathVariable Long id) {
    return reservationService.findById(id).map(ReservationController::toDto);
  }

  @PostMapping
  public ReservationDto create(@RequestBody ReservationCreateRequest request) {
    return toDto(
        reservationService.createReservation(
            request.clientId(), request.seanceId(), request.items()));
  }

  @PutMapping("/{id}/pay")
  public ReservationDto pay(@PathVariable Long id) {
    return toDto(reservationService.pay(id));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    reservationService.delete(id);
  }

  private static ReservationDto toDto(Reservation r) {
    Long clientId = r.getClient() != null ? r.getClient().getId() : null;
    Long seanceId = r.getSeance() != null ? r.getSeance().getId() : null;
    String statutCode = r.getStatut() != null ? r.getStatut().getCode() : null;
    return new ReservationDto(
        r.getId(),
        clientId != null ? new ClientRef(clientId) : null,
        seanceId != null ? new SeanceRef(seanceId) : null,
        statutCode,
        r.getNbPlace(),
        r.getMontantTotal(),
        r.getDateReservation(),
        r.getDateExpiration());
  }

  public record ClientRef(Long id) {}

  public record SeanceRef(Long id) {}

  public record ReservationDto(
      Long id,
      ClientRef client,
      SeanceRef seance,
      String statut,
      Integer nbPlace,
      java.math.BigDecimal montantTotal,
      java.time.Instant dateReservation,
      java.time.Instant dateExpiration) {}

  public record ReservationCreateRequest(
      Long clientId, Long seanceId, List<ReservationService.Item> items) {}
}
