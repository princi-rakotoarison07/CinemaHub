package com.cinema.management.repository;

import com.cinema.management.entity.ProduitTarif;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProduitTarifRepository extends JpaRepository<ProduitTarif, Long> {

  Optional<ProduitTarif> findByProduitExtraIdAndDateDebut(Long produitId, LocalDate dateDebut);

  @Query(
      "select t from ProduitTarif t "
          + "where t.produitExtra.id = :produitId "
          + "and t.dateDebut <= :d "
          + "and (t.dateFin is null or t.dateFin >= :d) "
          + "order by t.dateDebut desc")
  List<ProduitTarif> findApplicable(
      @Param("produitId") Long produitId,
      @Param("d") LocalDate d,
      Pageable pageable);
}
