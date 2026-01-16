package com.cinema.management.repository;

import com.cinema.management.entity.ConfigurationTarif;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationTarifRepository extends JpaRepository<ConfigurationTarif, Long> {

  @EntityGraph(attributePaths = {"tarif1", "tarif1.typePlace", "tarif1.categorieClient", "tarif2", "tarif2.typePlace", "tarif2.categorieClient"})
  List<ConfigurationTarif> findAll();

  @EntityGraph(attributePaths = {"tarif1", "tarif1.typePlace", "tarif1.categorieClient", "tarif2", "tarif2.typePlace", "tarif2.categorieClient"})
  Optional<ConfigurationTarif> findById(Long id);

  @EntityGraph(attributePaths = {"tarif1", "tarif1.typePlace", "tarif1.categorieClient", "tarif2", "tarif2.typePlace", "tarif2.categorieClient"})
  Optional<ConfigurationTarif> findFirstByTarif2IdAndActifTrue(Long tarif2Id);
}
