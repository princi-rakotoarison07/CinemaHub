package com.cinema.management.controller;

import com.cinema.management.entity.Tarif;
import com.cinema.management.service.TarifService;
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
@RequestMapping("/api/tarifs")
public class TarifController {

  private final TarifService tarifService;

  public TarifController(TarifService tarifService) {
    this.tarifService = tarifService;
  }

  @GetMapping
  public List<Tarif> getAll() {
    return tarifService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Tarif> getById(@PathVariable Long id) {
    return tarifService.findById(id);
  }

  @PostMapping
  public Tarif create(@RequestBody Tarif tarif) {
    return tarifService.create(tarif);
  }

  @PutMapping("/{id}")
  public Tarif update(@PathVariable Long id, @RequestBody Tarif tarif) {
    return tarifService.update(id, tarif);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    tarifService.delete(id);
  }
}
