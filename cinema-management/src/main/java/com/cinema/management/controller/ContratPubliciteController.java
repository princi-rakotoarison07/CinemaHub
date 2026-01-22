package com.cinema.management.controller;

import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.service.ContratPubliciteService;
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

  @GetMapping("/{id}")
  public Optional<ContratPublicite> getById(@PathVariable Long id) {
    return contratPubliciteService.findById(id);
  }

  @PostMapping
  public ContratPublicite create(@RequestBody ContratPublicite contrat) {
    return contratPubliciteService.create(contrat);
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
