package com.cinema.management.repository;

import com.cinema.management.entity.Seance;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeanceRepository extends JpaRepository<Seance, Long> {

  @EntityGraph(attributePaths = {"film", "film.genres", "salle"})
  List<Seance> findAll();

  @EntityGraph(attributePaths = {"film", "film.genres", "salle"})
  Optional<Seance> findById(Long id);

  @EntityGraph(attributePaths = {"film", "film.genres", "salle"})
  List<Seance> findByDateHeureBetween(Instant from, Instant to);
}
