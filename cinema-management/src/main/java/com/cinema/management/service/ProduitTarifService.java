package com.cinema.management.service;

import com.cinema.management.entity.ProduitExtra;
import com.cinema.management.entity.ProduitTarif;
import com.cinema.management.repository.ProduitExtraRepository;
import com.cinema.management.repository.ProduitTarifRepository;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ProduitTarifService {

  private final ProduitTarifRepository produitTarifRepository;
  private final ProduitExtraRepository produitExtraRepository;

  public ProduitTarifService(
      ProduitTarifRepository produitTarifRepository,
      ProduitExtraRepository produitExtraRepository) {
    this.produitTarifRepository = produitTarifRepository;
    this.produitExtraRepository = produitExtraRepository;
  }

  public ProduitTarif create(CreateProduitTarifRequest req) {
    if (req == null) throw new IllegalArgumentException("Requête obligatoire");
    if (req.idProduitExtra() == null) throw new IllegalArgumentException("Produit obligatoire");
    if (req.prix() == null) throw new IllegalArgumentException("Prix obligatoire");
    if (req.dateDebut() == null) throw new IllegalArgumentException("Date début obligatoire");
    if (req.dateFin() != null && req.dateFin().isBefore(req.dateDebut())) {
      throw new IllegalArgumentException("Date fin invalide");
    }

    ProduitExtra produit = produitExtraRepository.findById(req.idProduitExtra()).orElseThrow();

    ProduitTarif t =
        produitTarifRepository
            .findByProduitExtraIdAndDateDebut(produit.getId(), req.dateDebut())
            .orElseGet(ProduitTarif::new);

    t.setProduitExtra(produit);
    t.setPrix(req.prix());
    t.setDateDebut(req.dateDebut());
    t.setDateFin(req.dateFin());
    return produitTarifRepository.save(t);
  }

  public ProduitTarif findApplicableOrThrow(Long produitId, LocalDate date) {
    return produitTarifRepository
        .findApplicable(produitId, date, PageRequest.of(0, 1))
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Aucun tarif applicable pour ce produit"));
  }

  public record CreateProduitTarifRequest(
      Long idProduitExtra,
      java.math.BigDecimal prix,
      LocalDate dateDebut,
      LocalDate dateFin) {}
}
