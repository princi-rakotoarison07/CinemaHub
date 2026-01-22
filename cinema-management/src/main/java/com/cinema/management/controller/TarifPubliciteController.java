package com.cinema.management.controller;

import com.cinema.management.entity.TarifPublicite;
import com.cinema.management.service.TarifPubliciteService;
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
@RequestMapping("/api/tarifs-publicite")
public class TarifPubliciteController {

  private final TarifPubliciteService tarifPubliciteService;

  public TarifPubliciteController(TarifPubliciteService tarifPubliciteService) {
    this.tarifPubliciteService = tarifPubliciteService;
  }

  @GetMapping
  public List<TarifPublicite> getAll() {
    return tarifPubliciteService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<TarifPublicite> getById(@PathVariable Long id) {
    return tarifPubliciteService.findById(id);
  }

  @PostMapping
  public TarifPublicite create(@RequestBody TarifPublicite tarifPublicite) {
    return tarifPubliciteService.create(tarifPublicite);
  }

  @PutMapping("/{id}")
  public TarifPublicite update(@PathVariable Long id, @RequestBody TarifPublicite tarifPublicite) {
    return tarifPubliciteService.update(id, tarifPublicite);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    tarifPubliciteService.delete(id);
  }
}
