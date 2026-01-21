package com.cinema.management.service;

import com.cinema.management.entity.CategorieClient;
import com.cinema.management.entity.Client;
import com.cinema.management.entity.DetailsReservation;
import com.cinema.management.entity.Place;
import com.cinema.management.entity.Reservation;
import com.cinema.management.entity.Seance;
import com.cinema.management.entity.Statut;
import com.cinema.management.entity.Tarif;
import com.cinema.management.entity.Ticket;
import com.cinema.management.repository.ConfigurationTarifRepository;
import com.cinema.management.repository.CategorieClientRepository;
import com.cinema.management.repository.ClientRepository;
import com.cinema.management.repository.DetailsReservationRepository;
import com.cinema.management.repository.PlaceRepository;
import com.cinema.management.repository.ReservationRepository;
import com.cinema.management.repository.StatutRepository;
import com.cinema.management.repository.TarifRepository;
import com.cinema.management.repository.TicketRepository;
import com.cinema.management.repository.SeanceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
  private final DetailsReservationRepository detailsReservationRepository;
  private final ClientRepository clientRepository;
  private final SeanceRepository seanceRepository;
  private final PlaceRepository placeRepository;
  private final CategorieClientRepository categorieClientRepository;
  private final TarifRepository tarifRepository;
  private final ConfigurationTarifRepository configurationTarifRepository;
  private final StatutRepository statutRepository;

  public ReservationService(
      ReservationRepository reservationRepository,
      TicketRepository ticketRepository,
      DetailsReservationRepository detailsReservationRepository,
      ClientRepository clientRepository,
      SeanceRepository seanceRepository,
      PlaceRepository placeRepository,
      CategorieClientRepository categorieClientRepository,
      TarifRepository tarifRepository,
      ConfigurationTarifRepository configurationTarifRepository,
      StatutRepository statutRepository) {
    this.reservationRepository = reservationRepository;
    this.ticketRepository = ticketRepository;
    this.detailsReservationRepository = detailsReservationRepository;
    this.clientRepository = clientRepository;
    this.seanceRepository = seanceRepository;
    this.placeRepository = placeRepository;
    this.categorieClientRepository = categorieClientRepository;
    this.tarifRepository = tarifRepository;
    this.configurationTarifRepository = configurationTarifRepository;
    this.statutRepository = statutRepository;
  }

  private BigDecimal getAppliedPrice(Tarif baseTarif) {
    if (baseTarif == null || baseTarif.getId() == null) return BigDecimal.ZERO;

    return configurationTarifRepository
        .findFirstByTarif2IdAndActifTrue(baseTarif.getId())
        .map(cfg -> {
          BigDecimal basePrice = cfg.getTarif1() != null ? cfg.getTarif1().getPrix() : null;
          if (basePrice == null) return baseTarif.getPrix();

          BigDecimal pct = cfg.getPourcentage() != null ? cfg.getPourcentage() : BigDecimal.ZERO;
          BigDecimal applied =
              basePrice.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
          return applied;
        })
        .orElse(baseTarif.getPrix());
  }

  private Statut getStatutOrThrow(String code) {
    return statutRepository
        .findByCode(code)
        .orElseThrow(() -> new IllegalStateException("Statut introuvable: " + code));
  }

  public List<Reservation> findAll() {
    return reservationRepository.findAll();
  }

  public Optional<Reservation> findById(Long id) {
    return reservationRepository.findById(id);
  }

  public List<DetailsReservation> findDetailsByReservationId(Long reservationId) {
    return detailsReservationRepository.findByReservationId(reservationId);
  }

  @Transactional
  public Reservation pay(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
    String currentCode =
        reservation.getStatut() != null ? reservation.getStatut().getCode() : null;
    if ("ANNULEE".equals(currentCode)) {
      throw new IllegalStateException("Réservation annulée");
    }

    List<DetailsReservation> details = detailsReservationRepository.findByReservationId(reservationId);
    if (details == null || details.isEmpty()) {
      throw new IllegalStateException("Aucun détail de réservation");
    }

    List<Ticket> ticketsToCreate = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;
    for (DetailsReservation d : details) {
      if (Boolean.FALSE.equals(d.getIsActif())) continue;
      if (d.getPlace() == null || d.getPlace().getId() == null) continue;
      if (d.getCategorieClient() == null || d.getCategorieClient().getId() == null) continue;

      boolean alreadyTicketed =
          ticketRepository.existsByPlaceIdAndReservationSeanceIdAndReservationStatutCodeIn(
              d.getPlace().getId(),
              reservation.getSeance() != null ? reservation.getSeance().getId() : null,
              List.of("PAYEE"));
      if (alreadyTicketed) continue;

      Long typePlaceId =
          d.getPlace() != null && d.getPlace().getTypePlace() != null
              ? d.getPlace().getTypePlace().getId()
              : null;
      if (typePlaceId == null) {
        throw new IllegalStateException("Type de place introuvable");
      }

      Tarif tarif =
          tarifRepository
              .findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
                  typePlaceId, d.getCategorieClient().getId())
              .orElseThrow(() -> new IllegalStateException("Tarif introuvable"));

      BigDecimal appliedPrice = getAppliedPrice(tarif);

      Ticket t = new Ticket();
      t.setReservation(reservation);
      t.setPlace(d.getPlace());
      t.setCategorieClient(d.getCategorieClient());
      t.setPrix(appliedPrice);
      ticketsToCreate.add(t);

      total = total.add(appliedPrice);
    }

    if (!ticketsToCreate.isEmpty()) {
      ticketRepository.saveAll(ticketsToCreate);
    }

    reservation.setStatut(getStatutOrThrow("PAYEE"));
    reservation.setDateExpiration(null);
    reservation.setMontantTotal(total);
    return reservationRepository.save(reservation);
  }

  public PayPreview previewPay(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
    String currentCode =
        reservation.getStatut() != null ? reservation.getStatut().getCode() : null;
    if ("ANNULEE".equals(currentCode)) {
      throw new IllegalStateException("Réservation annulée");
    }

    List<DetailsReservation> details = detailsReservationRepository.findByReservationId(reservationId);
    if (details == null || details.isEmpty()) {
      throw new IllegalStateException("Aucun détail de réservation");
    }

    BigDecimal total = BigDecimal.ZERO;
    List<PayPreviewLine> lines = new ArrayList<>();

    for (DetailsReservation d : details) {
      if (Boolean.FALSE.equals(d.getIsActif())) continue;
      if (d.getPlace() == null || d.getPlace().getId() == null) continue;
      if (d.getCategorieClient() == null || d.getCategorieClient().getId() == null) continue;

      Long typePlaceId =
          d.getPlace() != null && d.getPlace().getTypePlace() != null
              ? d.getPlace().getTypePlace().getId()
              : null;
      if (typePlaceId == null) {
        throw new IllegalStateException("Type de place introuvable");
      }

      Tarif tarif =
          tarifRepository
              .findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
                  typePlaceId, d.getCategorieClient().getId())
              .orElseThrow(() -> new IllegalStateException("Tarif introuvable"));

      BigDecimal appliedPrice = getAppliedPrice(tarif);
      total = total.add(appliedPrice);

      lines.add(new PayPreviewLine(d.getPlace(), d.getCategorieClient(), appliedPrice));
    }

    return new PayPreview(reservationId, total, lines);
  }

  @Transactional
  public Reservation createReservation(Long clientId, Long seanceId, List<Item> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Aucune place sélectionnée");
    }

    int nbPlace = items.size();

    Client client = clientRepository.findById(clientId).orElseThrow();
    Seance seance = seanceRepository.findById(seanceId).orElseThrow();

    Reservation reservation = new Reservation();
    reservation.setClient(client);
    reservation.setSeance(seance);
    reservation.setStatut(getStatutOrThrow("EN_ATTENTE"));
    reservation.setNbPlace(nbPlace);
    reservation.setMontantTotal(BigDecimal.ZERO);
    reservation.setDateExpiration(Instant.now().plus(15, ChronoUnit.MINUTES));

    reservation = reservationRepository.save(reservation);

    List<DetailsReservation> details = new ArrayList<>();

    for (Item item : items) {
      Place place = placeRepository.findById(item.placeId()).orElseThrow();
      CategorieClient categorie =
          categorieClientRepository.findById(item.categorieClientId()).orElseThrow();

      boolean occupee =
          detailsReservationRepository.existsByPlaceIdAndReservationSeanceIdAndIsActifTrueAndReservationStatutCodeIn(
              place.getId(), seance.getId(), STATUTS_OCCUPES);
      if (occupee) {
        throw new IllegalStateException("Place déjà réservée pour cette séance");
      }

      Long typePlaceId = place.getTypePlace().getId();
      tarifRepository
          .findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
              typePlaceId, categorie.getId())
          .orElseThrow(() -> new IllegalStateException("Tarif introuvable"));

      DetailsReservation detail = new DetailsReservation();
      detail.setReservation(reservation);
      detail.setPlace(place);
      detail.setCategorieClient(categorie);

      details.add(detail);
    }

    detailsReservationRepository.saveAll(details);

    reservation.setMontantTotal(BigDecimal.ZERO);
    return reservationRepository.save(reservation);
  }

  @Transactional
  public Reservation updateReservation(Long id, List<Item> items) {
    Reservation reservation = reservationRepository.findById(id).orElseThrow();
    if ("PAYEE".equals(reservation.getStatut().getCode())
        || "ANNULEE".equals(reservation.getStatut().getCode())) {
      throw new IllegalStateException("Impossible de modifier une réservation payée ou annulée");
    }

    // 1. Charger tous les détails existants (actifs et inactifs)
    List<DetailsReservation> existingDetails = detailsReservationRepository.findByReservationId(id);

    // 2. Marquer tout comme inactif temporairement en mémoire pour identifier ce qui reste
    for (DetailsReservation d : existingDetails) {
      d.setIsActif(false);
    }

    // 3. Traiter les nouveaux items (sélection actuelle du front)
    for (Item item : items) {
      Place place = placeRepository.findById(item.placeId()).orElseThrow();
      CategorieClient categorie =
          categorieClientRepository.findById(item.categorieClientId()).orElseThrow();

      // Vérifier si la place est occupée par une AUTRE réservation active
      boolean occupee =
          detailsReservationRepository
              .existsByPlaceIdAndReservationSeanceIdAndIsActifTrueAndReservationStatutCodeInAndReservationIdNot(
                  place.getId(), reservation.getSeance().getId(), STATUTS_OCCUPES, id);

      if (occupee) {
        throw new IllegalStateException("La place " + place.getRangee() + place.getNumero() + " est déjà réservée par un autre client");
      }

      // Chercher si on a déjà un détail pour cette place dans cette réservation (même inactif)
      Optional<DetailsReservation> existing =
          existingDetails.stream().filter(d -> d.getPlace().getId().equals(place.getId())).findFirst();

      if (existing.isPresent()) {
        // C'est une place qu'on garde ou qu'on réactive
        DetailsReservation d = existing.get();
        d.setIsActif(true);
        d.setCategorieClient(categorie); // Mise à jour de la catégorie si changée
      } else {
        // C'est une nouvelle place ajoutée à la réservation
        DetailsReservation d = new DetailsReservation();
        d.setReservation(reservation);
        d.setPlace(place);
        d.setCategorieClient(categorie);
        d.setIsActif(true);
        existingDetails.add(d);
      }
    }

    // 4. Sauvegarder tous les détails (ceux restés isActif=false sont "annulés")
    detailsReservationRepository.saveAll(existingDetails);

    // 5. Mettre à jour l'en-tête de la réservation
    long activeCount = existingDetails.stream().filter(DetailsReservation::getIsActif).count();
    reservation.setNbPlace((int) activeCount);
    reservation.setMontantTotal(BigDecimal.ZERO); // Sera recalculé lors du passage en caisse (paiement)

    return reservationRepository.save(reservation);
  }

  public void delete(Long id) {
    reservationRepository.deleteById(id);
  }

  public record Item(Long placeId, Long categorieClientId) {}

  public record PayPreview(Long reservationId, BigDecimal total, List<PayPreviewLine> lines) {}

  public record PayPreviewLine(Place place, CategorieClient categorieClient, BigDecimal prix) {}
}
