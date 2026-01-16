package com.cinema.management.repository;

import com.cinema.management.entity.DetailsReservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailsReservationRepository extends JpaRepository<DetailsReservation, Long> {

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  List<DetailsReservation> findAll();

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  Optional<DetailsReservation> findById(Long id);

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "place.typePlace", "categorieClient"})
  List<DetailsReservation> findByReservationId(Long reservationId);
}
