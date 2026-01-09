package com.cinema.management.controller;

import com.cinema.management.entity.Place;
import com.cinema.management.service.PlaceService;
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
@RequestMapping("/api/places")
public class PlaceController {

  private final PlaceService placeService;

  public PlaceController(PlaceService placeService) {
    this.placeService = placeService;
  }

  @GetMapping
  public List<PlaceDto> getAll(@RequestParam(name = "salleId", required = false) Long salleId) {
    List<Place> list = salleId != null ? placeService.findBySalleId(salleId) : placeService.findAll();
    return list.stream().map(PlaceController::toDto).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public Optional<PlaceDto> getById(@PathVariable Long id) {
    return placeService.findById(id).map(PlaceController::toDto);
  }

  @PostMapping
  public Place create(@RequestBody Place place) {
    return placeService.create(place);
  }

  @PutMapping("/{id}")
  public Place update(@PathVariable Long id, @RequestBody Place place) {
    return placeService.update(id, place);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    placeService.delete(id);
  }

  private static PlaceDto toDto(Place p) {
    Long salleId = p.getSalle() != null ? p.getSalle().getId() : null;
    Long typePlaceId = p.getTypePlace() != null ? p.getTypePlace().getId() : null;
    return new PlaceDto(
        p.getId(),
        salleId != null ? new SalleRef(salleId) : null,
        p.getRangee(),
        p.getNumero(),
        typePlaceId != null ? new TypePlaceRef(typePlaceId) : null);
  }

  public record SalleRef(Long id) {}

  public record TypePlaceRef(Long id) {}

  public record PlaceDto(
      Long id,
      SalleRef salle,
      String rangee,
      Integer numero,
      TypePlaceRef typePlace) {}
}
