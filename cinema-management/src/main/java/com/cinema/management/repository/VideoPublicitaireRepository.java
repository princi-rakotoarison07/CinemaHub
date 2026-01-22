package com.cinema.management.repository;

import com.cinema.management.entity.VideoPublicitaire;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoPublicitaireRepository extends JpaRepository<VideoPublicitaire, Long> {

  @EntityGraph(attributePaths = {"societe"})
  List<VideoPublicitaire> findAll();

  @EntityGraph(attributePaths = {"societe"})
  Optional<VideoPublicitaire> findById(Long id);
}
