package com.cinema.management.repository;

import com.cinema.management.entity.Ticket;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  List<Ticket> findAll();

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  Optional<Ticket> findById(Long id);

  boolean existsByPlaceIdAndReservationSeanceIdAndReservationStatutIn(
      Long placeId, Long seanceId, Collection<String> statuts);

  @Query(
      "select distinct t.place.id "
          + "from Ticket t "
          + "join t.reservation r "
          + "where r.seance.id = :seanceId and r.statut in :statuts")
  List<Long> findOccupiedPlaceIdsBySeanceId(
      @Param("seanceId") Long seanceId, @Param("statuts") Collection<String> statuts);
}
