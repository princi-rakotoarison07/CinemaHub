package com.cinema.management.service;

import com.cinema.management.entity.CategorieClient;
import com.cinema.management.entity.Client;
import com.cinema.management.entity.Place;
import com.cinema.management.entity.Reservation;
import com.cinema.management.entity.Seance;
import com.cinema.management.entity.Tarif;
import com.cinema.management.entity.Ticket;
import com.cinema.management.repository.CategorieClientRepository;
import com.cinema.management.repository.ClientRepository;
import com.cinema.management.repository.PlaceRepository;
import com.cinema.management.repository.ReservationRepository;
import com.cinema.management.repository.TarifRepository;
import com.cinema.management.repository.TicketRepository;
import com.cinema.management.repository.SeanceRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

  private static final List<String> STATUTS_OCCUPES = List.of("EN_ATTENTE", "CONFIRMEE", "PAYEE");

  private final ReservationRepository reservationRepository;
  private final TicketRepository ticketRepository;
  private final ClientRepository clientRepository;
  private final SeanceRepository seanceRepository;
  private final PlaceRepository placeRepository;
  private final CategorieClientRepository categorieClientRepository;
  private final TarifRepository tarifRepository;

  public ReservationService(
      ReservationRepository reservationRepository,
      TicketRepository ticketRepository,
      ClientRepository clientRepository,
      SeanceRepository seanceRepository,
      PlaceRepository placeRepository,
      CategorieClientRepository categorieClientRepository,
      TarifRepository tarifRepository) {
    this.reservationRepository = reservationRepository;
    this.ticketRepository = ticketRepository;
    this.clientRepository = clientRepository;
    this.seanceRepository = seanceRepository;
    this.placeRepository = placeRepository;
    this.categorieClientRepository = categorieClientRepository;
    this.tarifRepository = tarifRepository;
  }

  public List<Reservation> findAll() {
    return reservationRepository.findAll();
  }

  public Optional<Reservation> findById(Long id) {
    return reservationRepository.findById(id);
  }

  @Transactional
  public Reservation createReservation(Long clientId, Long seanceId, List<Item> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Aucune place sélectionnée");
    }

    Client client = clientRepository.findById(clientId).orElseThrow();
    Seance seance = seanceRepository.findById(seanceId).orElseThrow();

    Reservation reservation = new Reservation();
    reservation.setClient(client);
    reservation.setSeance(seance);
    reservation.setStatut("EN_ATTENTE");
    reservation.setMontantTotal(BigDecimal.ZERO);
    reservation.setDateExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));

    reservation = reservationRepository.save(reservation);

    BigDecimal total = BigDecimal.ZERO;
    List<Ticket> tickets = new ArrayList<>();

    for (Item item : items) {
      Place place = placeRepository.findById(item.placeId()).orElseThrow();
      CategorieClient categorie =
          categorieClientRepository.findById(item.categorieClientId()).orElseThrow();

      boolean occupee =
          ticketRepository.existsByPlaceIdAndReservationSeanceIdAndReservationStatutIn(
              place.getId(), seance.getId(), STATUTS_OCCUPES);
      if (occupee) {
        throw new IllegalStateException("Place déjà réservée pour cette séance");
      }

      Long typePlaceId = place.getTypePlace().getId();
      Tarif tarif =
          tarifRepository
              .findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
                  typePlaceId, categorie.getId())
              .orElseThrow(() -> new IllegalStateException("Tarif introuvable"));

      Ticket ticket = new Ticket();
      ticket.setReservation(reservation);
      ticket.setPlace(place);
      ticket.setCategorieClient(categorie);
      ticket.setPrix(tarif.getPrix());

      tickets.add(ticket);
      total = total.add(tarif.getPrix());
    }

    ticketRepository.saveAll(tickets);

    reservation.setMontantTotal(total);
    return reservationRepository.save(reservation);
  }

  public void delete(Long id) {
    reservationRepository.deleteById(id);
  }

  public record Item(Long placeId, Long categorieClientId) {}
}
