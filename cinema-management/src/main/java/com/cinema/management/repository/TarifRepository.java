package com.cinema.management.repository;

import com.cinema.management.entity.Tarif;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifRepository extends JpaRepository<Tarif, Long> {

  @EntityGraph(attributePaths = {"typePlace", "categorieClient"})
  List<Tarif> findAll();

  @EntityGraph(attributePaths = {"typePlace", "categorieClient"})
  Optional<Tarif> findById(Long id);

  Optional<Tarif> findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
      Long typePlaceId, Long categorieClientId);
}
