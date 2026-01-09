package com.cinema.management.controller;

import com.cinema.management.entity.Salle;
import com.cinema.management.service.SalleService;
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
@RequestMapping("/api/salles")
public class SalleController {

  private final SalleService salleService;

  public SalleController(SalleService salleService) {
    this.salleService = salleService;
  }

  @GetMapping
  public List<Salle> getAll() {
    return salleService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Salle> getById(@PathVariable Long id) {
    return salleService.findById(id);
  }

  @PostMapping
  public Salle create(@RequestBody Salle salle) {
    return salleService.create(salle);
  }

  @PutMapping("/{id}")
  public Salle update(@PathVariable Long id, @RequestBody Salle salle) {
    return salleService.update(id, salle);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    salleService.delete(id);
  }
}
