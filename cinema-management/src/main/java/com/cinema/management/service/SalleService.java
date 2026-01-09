package com.cinema.management.service;

import com.cinema.management.entity.Salle;
import com.cinema.management.repository.SalleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SalleService {

  private final SalleRepository salleRepository;

  public SalleService(SalleRepository salleRepository) {
    this.salleRepository = salleRepository;
  }

  public List<Salle> findAll() {
    return salleRepository.findAll();
  }

  public Optional<Salle> findById(Long id) {
    return salleRepository.findById(id);
  }

  public Salle create(Salle salle) {
    return salleRepository.save(salle);
  }

  public Salle update(Long id, Salle salle) {
    Salle existing = salleRepository.findById(id).orElseThrow();
    existing.setNom(salle.getNom());
    existing.setCapacite(salle.getCapacite());
    return salleRepository.save(existing);
  }

  public void delete(Long id) {
    salleRepository.deleteById(id);
  }
}
