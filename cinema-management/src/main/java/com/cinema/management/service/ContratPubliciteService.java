package com.cinema.management.service;

import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.repository.ContratPubliciteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ContratPubliciteService {

  private final ContratPubliciteRepository contratPubliciteRepository;

  public ContratPubliciteService(ContratPubliciteRepository contratPubliciteRepository) {
    this.contratPubliciteRepository = contratPubliciteRepository;
  }

  public List<ContratPublicite> findAll() {
    return contratPubliciteRepository.findAll();
  }

  public Optional<ContratPublicite> findById(Long id) {
    return contratPubliciteRepository.findById(id);
  }

  public ContratPublicite create(ContratPublicite contrat) {
    return contratPubliciteRepository.save(contrat);
  }

  public ContratPublicite update(Long id, ContratPublicite contrat) {
    ContratPublicite existing = contratPubliciteRepository.findById(id).orElseThrow();
    existing.setSociete(contrat.getSociete());
    existing.setTarifPublicite(contrat.getTarifPublicite());
    existing.setNbDiffusions(contrat.getNbDiffusions());
    existing.setMontantTotal(contrat.getMontantTotal());
    existing.setDateDebut(contrat.getDateDebut());
    existing.setDateFin(contrat.getDateFin());
    existing.setActif(contrat.getActif());
    return contratPubliciteRepository.save(existing);
  }

  public void delete(Long id) {
    contratPubliciteRepository.deleteById(id);
  }
}
