package com.cinema.management.controller;

import com.cinema.management.entity.ProduitExtra;
import com.cinema.management.entity.ProduitTarif;
import com.cinema.management.service.ProduitExtraService;
import com.cinema.management.service.ProduitTarifService;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produits-extra")
public class ProduitExtraController {

  private final ProduitExtraService produitExtraService;
  private final ProduitTarifService produitTarifService;

  public ProduitExtraController(ProduitExtraService produitExtraService, ProduitTarifService produitTarifService) {
    this.produitExtraService = produitExtraService;
    this.produitTarifService = produitTarifService;
  }

  @GetMapping
  public List<ProduitExtra> getAll() {
    return produitExtraService.findAll();
  }

  @GetMapping("/with-prix")
  public List<ProduitExtraWithPrixDto> getAllWithPrix(
      @RequestParam(value = "date", required = false) LocalDate date) {
    LocalDate d = date != null ? date : LocalDate.now();
    return produitExtraService.findAll().stream()
        .map(p -> {
          java.math.BigDecimal prix = null;
          try {
            ProduitTarif t = produitTarifService.findApplicableOrThrow(p.getId(), d);
            prix = t != null ? t.getPrix() : null;
          } catch (Exception ignored) {
          }
          return new ProduitExtraWithPrixDto(p.getId(), p.getNom(), p.getDescription(), p.getActif(), prix);
        })
        .toList();
  }

  @GetMapping("/{id}")
  public Optional<ProduitExtra> getById(@PathVariable Long id) {
    return produitExtraService.findById(id);
  }

  @PostMapping
  public ProduitExtra create(@RequestBody ProduitExtra produit) {
    return produitExtraService.create(produit);
  }

  @PutMapping("/{id}")
  public ProduitExtra update(@PathVariable Long id, @RequestBody ProduitExtra produit) {
    return produitExtraService.update(id, produit);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    produitExtraService.delete(id);
  }

  public record ProduitExtraWithPrixDto(
      Long id, String nom, String description, Boolean actif, java.math.BigDecimal prix) {}
}
