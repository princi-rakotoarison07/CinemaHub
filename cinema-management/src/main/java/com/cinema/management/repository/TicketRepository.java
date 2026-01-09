package com.cinema.management.repository;

import com.cinema.management.entity.Ticket;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
  boolean existsByPlaceIdAndReservationSeanceIdAndReservationStatutIn(
      Long placeId, Long seanceId, Collection<String> statuts);
}
