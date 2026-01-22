package com.cinema.management.service;

import com.cinema.management.entity.TarifPublicite;
import com.cinema.management.repository.TarifPubliciteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TarifPubliciteService {

  private final TarifPubliciteRepository tarifPubliciteRepository;

  public TarifPubliciteService(TarifPubliciteRepository tarifPubliciteRepository) {
    this.tarifPubliciteRepository = tarifPubliciteRepository;
  }

  public List<TarifPublicite> findAll() {
    return tarifPubliciteRepository.findAll();
  }

  public Optional<TarifPublicite> findById(Long id) {
    return tarifPubliciteRepository.findById(id);
  }

  public TarifPublicite create(TarifPublicite tarifPublicite) {
    return tarifPubliciteRepository.save(tarifPublicite);
  }

  public TarifPublicite update(Long id, TarifPublicite tarifPublicite) {
    TarifPublicite existing = tarifPubliciteRepository.findById(id).orElseThrow();
    existing.setPrixParDiffusion(tarifPublicite.getPrixParDiffusion());
    existing.setDateDebut(tarifPublicite.getDateDebut());
    existing.setDateFin(tarifPublicite.getDateFin());
    existing.setActif(tarifPublicite.getActif());
    existing.setDescription(tarifPublicite.getDescription());
    return tarifPubliciteRepository.save(existing);
  }

  public void delete(Long id) {
    tarifPubliciteRepository.deleteById(id);
  }
}
