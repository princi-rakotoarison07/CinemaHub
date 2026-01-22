package com.cinema.management.controller;

import com.cinema.management.entity.PaiementContratPublicite;
import com.cinema.management.service.PaiementContratPubliciteService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/paiements-contrats-publicite")
public class PaiementContratPubliciteController {

  private final PaiementContratPubliciteService paiementService;

  public PaiementContratPubliciteController(PaiementContratPubliciteService paiementService) {
    this.paiementService = paiementService;
  }

  @GetMapping
  public List<PaiementContratPublicite> getAll(
      @RequestParam(value = "contratId", required = false) Long contratId,
      @RequestParam(value = "societeId", required = false) Long societeId) {
    if (contratId != null) return paiementService.findByContratId(contratId);
    if (societeId != null) return paiementService.findBySocieteId(societeId);
    return paiementService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<PaiementContratPublicite> getById(@PathVariable Long id) {
    return paiementService.findById(id);
  }

  @PostMapping
  public PaiementContratPublicite create(@RequestBody PaiementContratPublicite paiement) {
    try {
      return paiementService.create(paiement);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    paiementService.delete(id);
  }
}
