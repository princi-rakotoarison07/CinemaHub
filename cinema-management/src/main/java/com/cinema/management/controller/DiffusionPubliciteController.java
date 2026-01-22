package com.cinema.management.controller;

import com.cinema.management.entity.DiffusionPublicite;
import com.cinema.management.service.DiffusionPubliciteService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diffusions-publicite")
public class DiffusionPubliciteController {

  private final DiffusionPubliciteService service;

  public DiffusionPubliciteController(DiffusionPubliciteService service) {
    this.service = service;
  }

  @GetMapping
  public List<DiffusionPublicite> getByContrat(@RequestParam Long contratId) {
    return service.findByContratId(contratId);
  }
}
