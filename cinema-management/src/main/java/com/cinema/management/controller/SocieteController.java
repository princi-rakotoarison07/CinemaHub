package com.cinema.management.controller;

import com.cinema.management.entity.Societe;
import com.cinema.management.service.SocieteService;
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
@RequestMapping("/api/societes")
public class SocieteController {

  private final SocieteService societeService;

  public SocieteController(SocieteService societeService) {
    this.societeService = societeService;
  }

  @GetMapping
  public List<Societe> getAll() {
    return societeService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Societe> getById(@PathVariable Long id) {
    return societeService.findById(id);
  }

  @PostMapping
  public Societe create(@RequestBody Societe societe) {
    return societeService.create(societe);
  }

  @PutMapping("/{id}")
  public Societe update(@PathVariable Long id, @RequestBody Societe societe) {
    return societeService.update(id, societe);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    societeService.delete(id);
  }
}
