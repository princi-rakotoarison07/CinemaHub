package com.cinema.management.controller;

import com.cinema.management.entity.TypePlace;
import com.cinema.management.service.TypePlaceService;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/type-places")
public class TypePlaceController {

  private final TypePlaceService typePlaceService;

  public TypePlaceController(TypePlaceService typePlaceService) {
    this.typePlaceService = typePlaceService;
  }

  @GetMapping
  public List<TypePlace> getAll() {
    return typePlaceService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<TypePlace> getById(@PathVariable Long id) {
    return typePlaceService.findById(id);
  }
}
