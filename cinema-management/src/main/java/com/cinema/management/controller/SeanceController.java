package com.cinema.management.controller;

import com.cinema.management.entity.Seance;
import com.cinema.management.service.SeanceService;
import java.util.List;
import java.util.Optional;
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

  public SeanceController(SeanceService seanceService) {
    this.seanceService = seanceService;
  }

  @GetMapping
  public List<Seance> getAll() {
    return seanceService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Seance> getById(@PathVariable Long id) {
    return seanceService.findById(id);
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
}
