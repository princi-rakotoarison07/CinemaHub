package com.cinema.management.repository;

import com.cinema.management.entity.DetailsReservation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetailsReservationRepository extends JpaRepository<DetailsReservation, Long> {

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  List<DetailsReservation> findAll();

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  Optional<DetailsReservation> findById(Long id);

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "place.typePlace", "categorieClient"})
  List<DetailsReservation> findByReservationId(Long reservationId);

  boolean existsByPlaceIdAndReservationSeanceIdAndIsActifTrueAndReservationStatutCodeIn(
      Long placeId, Long seanceId, Collection<String> statuts);

  boolean existsByPlaceIdAndReservationSeanceIdAndIsActifTrueAndReservationStatutCodeInAndReservationIdNot(
      Long placeId, Long seanceId, Collection<String> statuts, Long excludeReservationId);

  @Query(
      "select distinct d.place.id "
          + "from DetailsReservation d "
          + "join d.reservation r "
          + "where r.seance.id = :seanceId and d.isActif = true and r.statut.code in :statuts")
  List<Long> findOccupiedPlaceIdsBySeanceId(
      @Param("seanceId") Long seanceId, @Param("statuts") Collection<String> statuts);
}
