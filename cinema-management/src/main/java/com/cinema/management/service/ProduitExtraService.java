package com.cinema.management.service;

import com.cinema.management.entity.ProduitExtra;
import com.cinema.management.repository.ProduitExtraRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProduitExtraService {

  private final ProduitExtraRepository produitExtraRepository;

  public ProduitExtraService(ProduitExtraRepository produitExtraRepository) {
    this.produitExtraRepository = produitExtraRepository;
  }

  public List<ProduitExtra> findAll() {
    return produitExtraRepository.findAll();
  }

  public Optional<ProduitExtra> findById(Long id) {
    return produitExtraRepository.findById(id);
  }

  public ProduitExtra create(ProduitExtra produit) {
    return produitExtraRepository.save(produit);
  }

  public ProduitExtra update(Long id, ProduitExtra produit) {
    ProduitExtra existing = produitExtraRepository.findById(id).orElseThrow();
    existing.setNom(produit.getNom());
    existing.setDescription(produit.getDescription());
    existing.setActif(produit.getActif());
    return produitExtraRepository.save(existing);
  }

  public void delete(Long id) {
    produitExtraRepository.deleteById(id);
  }
}
