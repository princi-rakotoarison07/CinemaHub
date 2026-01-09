package com.cinema.management.service;

import com.cinema.management.entity.TypePlace;
import com.cinema.management.repository.TypePlaceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TypePlaceService {

  private final TypePlaceRepository typePlaceRepository;

  public TypePlaceService(TypePlaceRepository typePlaceRepository) {
    this.typePlaceRepository = typePlaceRepository;
  }

  public List<TypePlace> findAll() {
    return typePlaceRepository.findAll();
  }

  public Optional<TypePlace> findById(Long id) {
    return typePlaceRepository.findById(id);
  }
}
