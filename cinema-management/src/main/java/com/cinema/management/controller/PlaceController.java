package com.cinema.management.controller;

import com.cinema.management.entity.Place;
import com.cinema.management.service.PlaceService;
import java.util.List;
import java.util.Optional;
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
  public List<Place> getAll(@RequestParam(name = "salleId", required = false) Long salleId) {
    if (salleId != null) {
      return placeService.findBySalleId(salleId);
    }
    return placeService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Place> getById(@PathVariable Long id) {
    return placeService.findById(id);
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
}
