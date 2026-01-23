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

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "place.typePlace", "categorieClient"})
  List<Ticket> findByReservationId(Long reservationId);

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  Optional<Ticket> findById(Long id);

  boolean existsByPlaceIdAndReservationSeanceIdAndReservationStatutCodeIn(
      Long placeId, Long seanceId, Collection<String> statuts);

  @Query(
      "select distinct t.place.id "
          + "from Ticket t "
          + "join t.reservation r "
          + "where r.seance.id = :seanceId and r.statut.code in :statuts")
  List<Long> findOccupiedPlaceIdsBySeanceId(
      @Param("seanceId") Long seanceId, @Param("statuts") Collection<String> statuts);

  @Query(
      "select r.seance.id as seanceId, sum(t.prix) as total "
          + "from Ticket t "
          + "join t.reservation r "
          + "where r.statut.code in :statuts "
          + "group by r.seance.id")
  List<SeanceRevenueProjection> sumPricesGroupBySeance(@Param("statuts") Collection<String> statuts);

  interface SeanceRevenueProjection {
    Long getSeanceId();
    java.math.BigDecimal getTotal();
  }

  @EntityGraph(attributePaths = {"reservation", "reservation.seance", "place", "categorieClient"})
  List<Ticket> findByReservationSeanceId(Long seanceId);
}
