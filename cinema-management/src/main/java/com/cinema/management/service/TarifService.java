package com.cinema.management.service;

import com.cinema.management.entity.Tarif;
import com.cinema.management.repository.TarifRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TarifService {

  private final TarifRepository tarifRepository;

  public TarifService(TarifRepository tarifRepository) {
    this.tarifRepository = tarifRepository;
  }

  public List<Tarif> findAll() {
    return tarifRepository.findAll();
  }

  public Optional<Tarif> findById(Long id) {
    return tarifRepository.findById(id);
  }

  public Tarif create(Tarif tarif) {
    return tarifRepository.save(tarif);
  }

  public Tarif update(Long id, Tarif tarif) {
    Tarif existing = tarifRepository.findById(id).orElseThrow();
    existing.setTypePlace(tarif.getTypePlace());
    existing.setCategorieClient(tarif.getCategorieClient());
    existing.setPrix(tarif.getPrix());
    existing.setActif(tarif.getActif());
    existing.setDateDebut(tarif.getDateDebut());
    existing.setDateFin(tarif.getDateFin());
    return tarifRepository.save(existing);
  }

  public void delete(Long id) {
    tarifRepository.deleteById(id);
  }
}
