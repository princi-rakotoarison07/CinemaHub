package com.cinema.management.controller;

import com.cinema.management.entity.Reservation;
import com.cinema.management.entity.DetailsReservation;
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
    return reservationService.findById(id).map(r -> {
      List<DetailsReservation> details = reservationService.findDetailsByReservationId(r.getId());
      return toDto(r, details);
    });
  }

  @PostMapping
  public ReservationDto create(@RequestBody ReservationCreateRequest request) {
    Reservation r = reservationService.createReservation(
            request.clientId(), request.seanceId(), request.items());
    List<DetailsReservation> details = reservationService.findDetailsByReservationId(r.getId());
    return toDto(r, details);
  }

  @PutMapping("/{id}")
  public ReservationDto update(@PathVariable Long id, @RequestBody ReservationCreateRequest request) {
    Reservation r = reservationService.updateReservation(id, request.items());
    List<DetailsReservation> details = reservationService.findDetailsByReservationId(r.getId());
    return toDto(r, details);
  }

  @PutMapping("/{id}/pay")
  public ReservationDto pay(@PathVariable Long id) {
    Reservation r = reservationService.pay(id);
    List<DetailsReservation> details = reservationService.findDetailsByReservationId(r.getId());
    return toDto(r, details);
  }

  @GetMapping("/{id}/pay-preview")
  public PayPreviewDto payPreview(@PathVariable Long id) {
    return toPayPreviewDto(reservationService.previewPay(id));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    reservationService.delete(id);
  }

  private static ReservationDto toDto(Reservation r) {
    return toDto(r, List.of());
  }

  private static ReservationDto toDto(Reservation r, List<DetailsReservation> details) {
    Long clientId = r.getClient() != null ? r.getClient().getId() : null;
    Long seanceId = r.getSeance() != null ? r.getSeance().getId() : null;
    String statutCode = r.getStatut() != null ? r.getStatut().getCode() : null;

    List<DetailsItemDto> items = details.stream()
        .filter(d -> d.getIsActif() != null && d.getIsActif())
        .map(d -> toDetailsItemDto(d))
        .collect(Collectors.toList());

    return new ReservationDto(
        r.getId(),
        clientId != null ? new ClientRef(clientId) : null,
        seanceId != null ? new SeanceRef(seanceId) : null,
        statutCode,
        r.getNbPlace(),
        r.getMontantTotal(),
        r.getDateReservation(),
        r.getDateExpiration(),
        items);
  }

  private static DetailsItemDto toDetailsItemDto(DetailsReservation d) {
    Long placeId = d.getPlace() != null ? d.getPlace().getId() : null;
    String rangee = d.getPlace() != null ? d.getPlace().getRangee() : null;
    Integer numero = d.getPlace() != null ? d.getPlace().getNumero() : null;
    Long typePlaceId = d.getPlace() != null && d.getPlace().getTypePlace() != null ? d.getPlace().getTypePlace().getId() : null;
    Long catId = d.getCategorieClient() != null ? d.getCategorieClient().getId() : null;
    
    return new DetailsItemDto(
        d.getId(),
        placeId != null ? new PlaceRef(placeId, rangee, numero, rangee + numero, typePlaceId, null) : null,
        catId != null ? new CategorieClientRef(catId, d.getCategorieClient().getLibelle()) : null
    );
  }

  public record ClientRef(Long id) {}

  public record SeanceRef(Long id) {}

  public record DetailsItemDto(Long id, PlaceRef place, CategorieClientRef categorieClient) {}

  public record ReservationDto(
      Long id,
      ClientRef client,
      SeanceRef seance,
      String statut,
      Integer nbPlace,
      java.math.BigDecimal montantTotal,
      java.time.Instant dateReservation,
      java.time.Instant dateExpiration,
      List<DetailsItemDto> items) {}

  public record ReservationCreateRequest(
      Long clientId, Long seanceId, List<ReservationService.Item> items) {}

  private static PayPreviewDto toPayPreviewDto(ReservationService.PayPreview p) {
    List<PayPreviewLineDto> lines =
        p.lines().stream().map(l -> toPayPreviewLineDto(l)).collect(Collectors.toList());
    return new PayPreviewDto(p.reservationId(), p.total(), lines);
  }

  private static PayPreviewLineDto toPayPreviewLineDto(ReservationService.PayPreviewLine l) {
    Long placeId = l.place() != null ? l.place().getId() : null;
    String rangee = l.place() != null ? l.place().getRangee() : null;
    Integer numero = l.place() != null ? l.place().getNumero() : null;
    String placeLabel =
        (rangee != null && numero != null) ? (rangee + numero) : (placeId != null ? String.valueOf(placeId) : null);

    Long typePlaceId =
        l.place() != null && l.place().getTypePlace() != null ? l.place().getTypePlace().getId() : null;
    String typePlaceLibelle =
        l.place() != null && l.place().getTypePlace() != null ? l.place().getTypePlace().getLibelle() : null;

    Long categorieId = l.categorieClient() != null ? l.categorieClient().getId() : null;
    String categorieLibelle = l.categorieClient() != null ? l.categorieClient().getLibelle() : null;

    return new PayPreviewLineDto(
        placeId != null ? new PlaceRef(placeId, rangee, numero, placeLabel, typePlaceId, typePlaceLibelle) : null,
        categorieId != null ? new CategorieClientRef(categorieId, categorieLibelle) : null,
        l.prix());
  }

  public record PlaceRef(Long id, String rangee, Integer numero, String label, Long typePlaceId, String typePlaceLibelle) {}

  public record CategorieClientRef(Long id, String libelle) {}

  public record PayPreviewLineDto(PlaceRef place, CategorieClientRef categorieClient, java.math.BigDecimal prix) {}

  public record PayPreviewDto(Long reservationId, java.math.BigDecimal total, List<PayPreviewLineDto> lines) {}
}
