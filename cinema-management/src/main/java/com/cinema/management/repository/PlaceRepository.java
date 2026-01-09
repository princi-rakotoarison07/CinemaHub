package com.cinema.management.repository;

import com.cinema.management.entity.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

  @EntityGraph(attributePaths = {"salle", "typePlace"})
  List<Place> findAll();

  @EntityGraph(attributePaths = {"salle", "typePlace"})
  Optional<Place> findById(Long id);

  @EntityGraph(attributePaths = {"salle", "typePlace"})
  List<Place> findBySalleId(Long salleId);
}
