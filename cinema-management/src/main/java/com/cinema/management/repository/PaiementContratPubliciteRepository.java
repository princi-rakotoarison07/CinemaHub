package com.cinema.management.repository;

import com.cinema.management.entity.PaiementContratPublicite;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaiementContratPubliciteRepository extends JpaRepository<PaiementContratPublicite, Long> {

  @EntityGraph(
      attributePaths = {
        "contratPublicite",
        "contratPublicite.videoPublicitaire",
        "contratPublicite.videoPublicitaire.societe",
        "contratPublicite.tarifPublicite"
      })
  List<PaiementContratPublicite> findAll();

  @EntityGraph(
      attributePaths = {
        "contratPublicite",
        "contratPublicite.videoPublicitaire",
        "contratPublicite.videoPublicitaire.societe",
        "contratPublicite.tarifPublicite"
      })
  List<PaiementContratPublicite> findByContratPubliciteId(Long contratId);

  @EntityGraph(
      attributePaths = {
        "contratPublicite",
        "contratPublicite.videoPublicitaire",
        "contratPublicite.videoPublicitaire.societe",
        "contratPublicite.tarifPublicite"
      })
  @Query(
      "select p from PaiementContratPublicite p where p.contratPublicite.videoPublicitaire.societe.id = :societeId")
  List<PaiementContratPublicite> findBySocieteId(@Param("societeId") Long societeId);

  @Query("select coalesce(sum(p.montant), 0) from PaiementContratPublicite p where p.contratPublicite.id = :contratId")
  BigDecimal sumByContratId(@Param("contratId") Long contratId);
}
