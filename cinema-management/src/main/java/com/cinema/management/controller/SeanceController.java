package com.cinema.management.controller;

import com.cinema.management.entity.Seance;
import com.cinema.management.entity.Place;
import com.cinema.management.service.SeanceService;
import com.cinema.management.service.PlaceService;
import com.cinema.management.repository.TicketRepository;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.HashSet;
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
@RequestMapping("/api/seances")
public class SeanceController {

  private final SeanceService seanceService;
  private final PlaceService placeService;
  private final TicketRepository ticketRepository;

  private static final List<String> STATUTS_OCCUPES = List.of("EN_ATTENTE", "CONFIRMEE", "PAYEE");

  public SeanceController(SeanceService seanceService, PlaceService placeService, TicketRepository ticketRepository) {
    this.seanceService = seanceService;
    this.placeService = placeService;
    this.ticketRepository = ticketRepository;
  }

  @GetMapping
  public List<SeanceDto> getAll() {
    return seanceService.findAll().stream().map(SeanceController::toDto).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public Optional<SeanceDto> getById(@PathVariable Long id) {
    return seanceService.findById(id).map(SeanceController::toDto);
  }

  @PostMapping
  public Seance create(@RequestBody Seance seance) {
    return seanceService.create(seance);
  }

  @PutMapping("/{id}")
  public Seance update(@PathVariable Long id, @RequestBody Seance seance) {
    return seanceService.update(id, seance);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    seanceService.delete(id);
  }

  @GetMapping("/{id}/places")
  public List<SeancePlaceDto> getPlacesForSeance(@PathVariable Long id) {
    Seance seance = seanceService.findById(id).orElseThrow();
    Long salleId = seance.getSalle() != null ? seance.getSalle().getId() : null;
    if (salleId == null) {
      return List.of();
    }

    List<Place> places = placeService.findBySalleId(salleId);
    Collection<Long> occupiedIds =
        new HashSet<>(ticketRepository.findOccupiedPlaceIdsBySeanceId(id, STATUTS_OCCUPES));

    return places.stream()
        .map(p -> {
          Long typePlaceId = p.getTypePlace() != null ? p.getTypePlace().getId() : null;
          boolean occupee = occupiedIds.contains(p.getId());
          String label = (p.getRangee() != null ? p.getRangee() : "") + (p.getNumero() != null ? p.getNumero() : "");
          return new SeancePlaceDto(p.getId(), p.getRangee(), p.getNumero(), label, typePlaceId, occupee);
        })
        .collect(Collectors.toList());
  }

  private static SeanceDto toDto(Seance s) {
    Long filmId = s.getFilm() != null ? s.getFilm().getId() : null;
    Long salleId = s.getSalle() != null ? s.getSalle().getId() : null;
    return new SeanceDto(
        s.getId(),
        s.getDateHeure(),
        s.getLangue(),
        s.getVersion(),
        filmId != null ? new FilmRef(filmId) : null,
        salleId != null ? new SalleRef(salleId) : null);
  }

  public record FilmRef(Long id) {}

  public record SalleRef(Long id) {}

  public record SeanceDto(
      Long id,
      java.time.Instant dateHeure,
      String langue,
      String version,
      FilmRef film,
      SalleRef salle) {}

  public record SeancePlaceDto(
      Long id,
      String rangee,
      Integer numero,
      String label,
      Long typePlaceId,
      boolean occupee) {}
}
