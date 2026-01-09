package com.cinema.management.repository;

import com.cinema.management.entity.Reservation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @EntityGraph(attributePaths = {"client", "seance", "seance.film", "seance.film.genres", "seance.salle"})
  List<Reservation> findAll();

  @EntityGraph(attributePaths = {"client", "seance", "seance.film", "seance.film.genres", "seance.salle"})
  Optional<Reservation> findById(Long id);
}
