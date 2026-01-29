package com.cinema.management.controller;

import com.cinema.management.entity.Vente;
import com.cinema.management.entity.VenteLigne;
import com.cinema.management.service.VenteService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ventes")
public class VenteController {

  private final VenteService venteService;

  public VenteController(VenteService venteService) {
    this.venteService = venteService;
  }

  @GetMapping
  public List<VenteDto> getAll() {
    return venteService.findAll().stream().map(VenteController::toDto).collect(Collectors.toList());
  }

  @PostMapping
  public VenteDto create(@RequestBody VenteService.CreateVenteRequest req) {
    try {
      Vente v = venteService.create(req);
      List<VenteLigne> lignes = venteService.findLignes(v.getId());
      return toDto(v, lignes);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    venteService.delete(id);
  }

  @GetMapping("/{id}/lignes")
  public VenteDetailsDto getLignes(@PathVariable Long id) {
    try {
      VenteService.VenteDetails details = venteService.getDetails(id);
      return new VenteDetailsDto(
          details.id(),
          details.montantTotal(),
          details.lignes().stream().map(VenteController::toDto).collect(Collectors.toList()));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/lignes/{ligneId}")
  public VenteDetailsDto updateLigne(
      @PathVariable Long ligneId, @RequestBody VenteService.UpdateVenteLigneRequest req) {
    try {
      VenteService.VenteDetails details =
          venteService.updateLigneQuantite(ligneId, req != null ? req.quantite() : null);
      return new VenteDetailsDto(
          details.id(),
          details.montantTotal(),
          details.lignes().stream().map(VenteController::toDto).collect(Collectors.toList()));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @DeleteMapping("/lignes/{ligneId}")
  public VenteDetailsDto deleteLigne(@PathVariable Long ligneId) {
    try {
      VenteService.VenteDetails details = venteService.deleteLigne(ligneId);
      return new VenteDetailsDto(
          details.id(),
          details.montantTotal(),
          details.lignes().stream().map(VenteController::toDto).collect(Collectors.toList()));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  private static VenteDto toDto(Vente v) {
    return toDto(v, List.of());
  }

  private static VenteDto toDto(Vente v, List<VenteLigne> lignes) {
    Long clientId = v.getClient() != null ? v.getClient().getId() : null;

    List<VenteLigneDto> lineDtos =
        lignes.stream().map(VenteController::toDto).collect(Collectors.toList());

    return new VenteDto(v.getId(), clientId != null ? new ClientRef(clientId) : null, v.getDateVente(), v.getMontantTotal(), lineDtos);
  }

  private static VenteLigneDto toDto(VenteLigne l) {
    Long produitId = l.getProduitExtra() != null ? l.getProduitExtra().getId() : null;
    String produitNom = l.getProduitExtra() != null ? l.getProduitExtra().getNom() : null;

    return new VenteLigneDto(
        l.getId(),
        produitId != null ? new ProduitExtraRef(produitId, produitNom) : null,
        l.getQuantite(),
        l.getPrixUnitaire());
  }

  public record ClientRef(Long id) {}

  public record ProduitExtraRef(Long id, String nom) {}

  public record VenteLigneDto(Long id, ProduitExtraRef produit, Integer quantite, java.math.BigDecimal prixUnitaire) {}

  public record VenteDetailsDto(Long id, java.math.BigDecimal montantTotal, List<VenteLigneDto> lignes) {}

  public record VenteDto(
      Long id,
      ClientRef client,
      java.time.Instant dateVente,
      java.math.BigDecimal montantTotal,
      List<VenteLigneDto> lignes) {}
}
