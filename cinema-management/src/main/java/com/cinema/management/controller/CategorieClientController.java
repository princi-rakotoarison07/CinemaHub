package com.cinema.management.controller;

import com.cinema.management.entity.CategorieClient;
import com.cinema.management.service.CategorieClientService;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories-clients")
public class CategorieClientController {

  private final CategorieClientService categorieClientService;

  public CategorieClientController(CategorieClientService categorieClientService) {
    this.categorieClientService = categorieClientService;
  }

  @GetMapping
  public List<CategorieClient> getAll() {
    return categorieClientService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<CategorieClient> getById(@PathVariable Long id) {
    return categorieClientService.findById(id);
  }
}
