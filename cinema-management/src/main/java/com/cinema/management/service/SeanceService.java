package com.cinema.management.service;

import com.cinema.management.entity.Seance;
import com.cinema.management.repository.SeanceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SeanceService {

  private final SeanceRepository seanceRepository;

  public SeanceService(SeanceRepository seanceRepository) {
    this.seanceRepository = seanceRepository;
  }

  public List<Seance> findAll() {
    return seanceRepository.findAll();
  }

  public Optional<Seance> findById(Long id) {
    return seanceRepository.findById(id);
  }

  public Seance create(Seance seance) {
    return seanceRepository.save(seance);
  }

  public Seance update(Long id, Seance seance) {
    Seance existing = seanceRepository.findById(id).orElseThrow();
    existing.setFilm(seance.getFilm());
    existing.setSalle(seance.getSalle());
    existing.setDateHeure(seance.getDateHeure());
    existing.setLangue(seance.getLangue());
    existing.setVersion(seance.getVersion());
    return seanceRepository.save(existing);
  }

  public void delete(Long id) {
    seanceRepository.deleteById(id);
  }
}
