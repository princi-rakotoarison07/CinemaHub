package com.cinema.management.service;

import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.entity.PaiementContratPublicite;
import com.cinema.management.repository.ContratPubliciteRepository;
import com.cinema.management.repository.PaiementContratPubliciteRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PaiementContratPubliciteService {

  private final PaiementContratPubliciteRepository paiementRepository;
  private final ContratPubliciteRepository contratRepository;

  public PaiementContratPubliciteService(
      PaiementContratPubliciteRepository paiementRepository,
      ContratPubliciteRepository contratRepository) {
    this.paiementRepository = paiementRepository;
    this.contratRepository = contratRepository;
  }

  public List<PaiementContratPublicite> findAll() {
    return paiementRepository.findAll();
  }

  public Optional<PaiementContratPublicite> findById(Long id) {
    return paiementRepository.findById(id);
  }

  public List<PaiementContratPublicite> findByContratId(Long contratId) {
    return paiementRepository.findByContratPubliciteId(contratId);
  }

  public List<PaiementContratPublicite> findBySocieteId(Long societeId) {
    return paiementRepository.findBySocieteId(societeId);
  }

  public PaiementContratPublicite create(PaiementContratPublicite paiement) {
    if (paiement == null) throw new IllegalArgumentException("Paiement obligatoire");
    if (paiement.getContratPublicite() == null || paiement.getContratPublicite().getId() == null) {
      throw new IllegalArgumentException("Contrat publicité obligatoire");
    }
    if (paiement.getMontant() == null || paiement.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Montant invalide");
    }

    ContratPublicite contrat = contratRepository.findById(paiement.getContratPublicite().getId()).orElseThrow();

    BigDecimal total = contrat.getMontantTotal() == null ? BigDecimal.ZERO : contrat.getMontantTotal();
    BigDecimal dejaPaye = paiementRepository.sumByContratId(contrat.getId());
    BigDecimal reste = total.subtract(dejaPaye);

    if (paiement.getMontant().compareTo(reste) > 0) {
      throw new IllegalArgumentException("Montant dépasse le reste à payer");
    }

    paiement.setContratPublicite(contrat);
    return paiementRepository.save(paiement);
  }

  public void delete(Long id) {
    paiementRepository.deleteById(id);
  }
}
