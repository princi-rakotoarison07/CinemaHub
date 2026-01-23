package com.cinema.management.repository;

import com.cinema.management.entity.DiffusionPublicite;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiffusionPubliciteRepository extends JpaRepository<DiffusionPublicite, Long> {

  @EntityGraph(attributePaths = {"contratPublicite", "seance"})
  List<DiffusionPublicite> findByContratPubliciteId(Long contratId);

  @EntityGraph(
      attributePaths = {
        "contratPublicite",
        "contratPublicite.tarifPublicite",
        "contratPublicite.videoPublicitaire",
        "contratPublicite.videoPublicitaire.societe",
        "seance",
        "seance.film"
      })
  List<DiffusionPublicite> findWithDetailsByContratPubliciteId(Long contratId);

  @EntityGraph(
      attributePaths = {
        "contratPublicite",
        "contratPublicite.tarifPublicite",
        "contratPublicite.videoPublicitaire",
        "contratPublicite.videoPublicitaire.societe",
        "seance",
        "seance.film"
      })
  List<DiffusionPublicite> findBySeanceId(Long seanceId);

  @Query(
      "select dp.seance.id, coalesce(sum(dp.contratPublicite.tarifPublicite.prixParDiffusion * dp.nombrePub), 0) "
          + "from DiffusionPublicite dp "
          + "where dp.seance.id in :seanceIds "
          + "group by dp.seance.id")
  List<Object[]> sumPubliciteBySeanceIds(@Param("seanceIds") List<Long> seanceIds);
}
