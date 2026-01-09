package com.cinema.management.repository;

import com.cinema.management.entity.Tarif;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifRepository extends JpaRepository<Tarif, Long> {
  Optional<Tarif> findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
      Long typePlaceId, Long categorieClientId);
}
