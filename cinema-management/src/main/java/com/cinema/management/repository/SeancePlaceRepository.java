package com.cinema.management.repository;

import com.cinema.management.entity.SeancePlace;
import com.cinema.management.entity.SeancePlaceId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeancePlaceRepository extends JpaRepository<SeancePlace, SeancePlaceId> {

  @EntityGraph(attributePaths = {"place", "place.salle", "typePlace"})
  List<SeancePlace> findBySeanceId(Long seanceId);

  @EntityGraph(attributePaths = {"typePlace"})
  Optional<SeancePlace> findByIdSeanceIdAndIdPlaceId(Long seanceId, Long placeId);
}
