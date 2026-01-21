package com.cinema.management.controller;

import com.cinema.management.entity.Ticket;
import com.cinema.management.service.TicketService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @GetMapping
  public List<TicketDto> getAll(
      @RequestParam(name = "reservationId", required = false) Long reservationId) {
    List<Ticket> list =
        reservationId != null
            ? ticketService.findByReservationId(reservationId)
            : ticketService.findAll();
    return list.stream().map(TicketController::toDto).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public Optional<TicketDto> getById(@PathVariable Long id) {
    return ticketService.findById(id).map(TicketController::toDto);
  }

  @PostMapping
  public Ticket create(@RequestBody Ticket ticket) {
    return ticketService.create(ticket);
  }

  @PutMapping("/{id}")
  public Ticket update(@PathVariable Long id, @RequestBody Ticket ticket) {
    return ticketService.update(id, ticket);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    ticketService.delete(id);
  }

  private static TicketDto toDto(Ticket t) {
    Long reservationId = t.getReservation() != null ? t.getReservation().getId() : null;
    Long placeId = t.getPlace() != null ? t.getPlace().getId() : null;
    Long categorieId = t.getCategorieClient() != null ? t.getCategorieClient().getId() : null;

    String rangee = t.getPlace() != null ? t.getPlace().getRangee() : null;
    Integer numero = t.getPlace() != null ? t.getPlace().getNumero() : null;
    String placeLabel =
        (rangee != null && numero != null) ? (rangee + numero) : (placeId != null ? String.valueOf(placeId) : null);

    Long typePlaceId =
        t.getPlace() != null && t.getPlace().getTypePlace() != null ? t.getPlace().getTypePlace().getId() : null;
    String typePlaceLibelle =
        t.getPlace() != null && t.getPlace().getTypePlace() != null ? t.getPlace().getTypePlace().getLibelle() : null;

    String categorieLibelle = t.getCategorieClient() != null ? t.getCategorieClient().getLibelle() : null;

    return new TicketDto(
        t.getId(),
        reservationId != null ? new ReservationRef(reservationId) : null,
        placeId != null ? new PlaceRef(placeId, rangee, numero, placeLabel, typePlaceId, typePlaceLibelle) : null,
        categorieId != null ? new CategorieClientRef(categorieId, categorieLibelle) : null,
        t.getPrix());
  }

  public record ReservationRef(Long id) {}

  public record PlaceRef(Long id, String rangee, Integer numero, String label, Long typePlaceId, String typePlaceLibelle) {}

  public record CategorieClientRef(Long id, String libelle) {}

  public record TicketDto(
      Long id,
      ReservationRef reservation,
      PlaceRef place,
      CategorieClientRef categorieClient,
      java.math.BigDecimal prix) {}
}
