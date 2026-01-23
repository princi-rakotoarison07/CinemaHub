package com.cinema.management.controller;

import com.cinema.management.dto.CreateContratPubliciteRequest;
import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.service.ContratPubliciteService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/contrats-publicite")
public class ContratPubliciteController {

  private final ContratPubliciteService contratPubliciteService;

  public ContratPubliciteController(ContratPubliciteService contratPubliciteService) {
    this.contratPubliciteService = contratPubliciteService;
  }

  @GetMapping
  public List<ContratPublicite> getAll() {
    return contratPubliciteService.findAll();
  }

  @GetMapping("/factures")
  public List<ContratPubliciteService.ContratPubliciteFactureDto> getFactures() {
    return contratPubliciteService.getFacturesContrats();
  }

  @GetMapping("/{id}/facture-details")
  public ContratPubliciteService.ContratPubliciteFactureDetailsDto getFactureDetails(@PathVariable Long id) {
    try {
      return contratPubliciteService.getFactureDetails(id);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @GetMapping("/{id}")
  public Optional<ContratPublicite> getById(@PathVariable Long id) {
    return contratPubliciteService.findById(id);
  }

  @PostMapping
  public ContratPublicite create(@RequestBody ContratPublicite contrat) {
    return contratPubliciteService.create(contrat);
  }

  @PostMapping("/with-diffusions")
  public ContratPublicite createWithDiffusions(@RequestBody CreateContratPubliciteRequest req) {
    try {
      return contratPubliciteService.createWithDiffusions(req);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ContratPublicite update(@PathVariable Long id, @RequestBody ContratPublicite contrat) {
    return contratPubliciteService.update(id, contrat);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    contratPubliciteService.delete(id);
  }
}
