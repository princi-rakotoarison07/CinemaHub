package com.cinema.management.controller;

import com.cinema.management.entity.Reservation;
import com.cinema.management.service.ReservationService;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
  public List<Reservation> getAll() {
    return reservationService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Reservation> getById(@PathVariable Long id) {
    return reservationService.findById(id);
  }

  @PostMapping
  public Reservation create(@RequestBody ReservationCreateRequest request) {
    return reservationService.createReservation(
        request.clientId(), request.seanceId(), request.items());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    reservationService.delete(id);
  }

  public record ReservationCreateRequest(
      Long clientId, Long seanceId, List<ReservationService.Item> items) {}
}
