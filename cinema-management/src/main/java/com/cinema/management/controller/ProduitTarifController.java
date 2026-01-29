package com.cinema.management.controller;

import com.cinema.management.entity.ProduitTarif;
import com.cinema.management.service.ProduitTarifService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/produit-tarifs")
public class ProduitTarifController {

  private final ProduitTarifService produitTarifService;

  public ProduitTarifController(ProduitTarifService produitTarifService) {
    this.produitTarifService = produitTarifService;
  }

  @PostMapping
  public ProduitTarif create(@RequestBody ProduitTarifService.CreateProduitTarifRequest req) {
    try {
      return produitTarifService.create(req);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }
}
