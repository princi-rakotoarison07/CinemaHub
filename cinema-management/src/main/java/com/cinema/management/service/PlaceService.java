package com.cinema.management.service;

import com.cinema.management.entity.Place;
import com.cinema.management.repository.PlaceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PlaceService {

  private final PlaceRepository placeRepository;

  public PlaceService(PlaceRepository placeRepository) {
    this.placeRepository = placeRepository;
  }

  public List<Place> findAll() {
    return placeRepository.findAll();
  }

  public Optional<Place> findById(Long id) {
    return placeRepository.findById(id);
  }

  public List<Place> findBySalleId(Long salleId) {
    return placeRepository.findBySalleId(salleId);
  }

  public Place create(Place place) {
    return placeRepository.save(place);
  }

  public Place update(Long id, Place place) {
    Place existing = placeRepository.findById(id).orElseThrow();
    existing.setSalle(place.getSalle());
    existing.setRangee(place.getRangee());
    existing.setNumero(place.getNumero());
    existing.setTypePlace(place.getTypePlace());
    return placeRepository.save(existing);
  }

  public void delete(Long id) {
    placeRepository.deleteById(id);
  }
}
