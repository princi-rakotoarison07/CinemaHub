package com.cinema.management.repository;

import com.cinema.management.entity.TarifPublicite;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface TarifPubliciteRepository extends JpaRepository<TarifPublicite, Long> {

  @Query(
      "select t from TarifPublicite t "
          + "where t.actif = true "
          + "and t.dateDebut <= :d "
          + "and (t.dateFin is null or t.dateFin >= :d) "
          + "order by t.dateDebut desc")
  List<TarifPublicite> findApplicable(@Param("d") LocalDate d, Pageable pageable);
}
