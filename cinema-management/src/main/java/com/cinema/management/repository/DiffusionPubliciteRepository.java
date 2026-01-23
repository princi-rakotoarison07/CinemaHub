package com.cinema.management.repository;

import com.cinema.management.entity.DiffusionPublicite;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiffusionPubliciteRepository extends JpaRepository<DiffusionPublicite, Long> {

  @EntityGraph(attributePaths = {"contratPublicite", "seance"})
  List<DiffusionPublicite> findByContratPubliciteId(Long contratId);

  @EntityGraph(attributePaths = {"contratPublicite", "seance"})
  List<DiffusionPublicite> findAll();

  @EntityGraph(attributePaths = {"contratPublicite", "seance"})
  List<DiffusionPublicite> findBySeanceId(Long seanceId);
}
