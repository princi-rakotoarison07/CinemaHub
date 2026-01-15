package com.cinema.management.service;

import com.cinema.management.entity.Seance;
import com.cinema.management.entity.Place;
import com.cinema.management.repository.SeanceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeanceService {

  private final SeanceRepository seanceRepository;
  private final PlaceService placeService;
  private final SeancePlaceService seancePlaceService;

  public SeanceService(
      SeanceRepository seanceRepository, PlaceService placeService, SeancePlaceService seancePlaceService) {
    this.seanceRepository = seanceRepository;
    this.placeService = placeService;
    this.seancePlaceService = seancePlaceService;
  }

  public List<Seance> findAll() {
    return seanceRepository.findAll();
  }

  public Optional<Seance> findById(Long id) {
    return seanceRepository.findById(id);
  }

  @Transactional
  public Seance create(Seance seance) {
    Seance saved = seanceRepository.save(seance);

    Long salleId = saved.getSalle() != null ? saved.getSalle().getId() : null;
    if (salleId != null) {
      List<Place> places = placeService.findBySalleId(salleId);
      seancePlaceService.initFromPlaces(saved, places);
    }

    return saved;
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
