package com.cinema.management.service;

import com.cinema.management.dto.SeanceRevenueDto;
import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.entity.DiffusionPublicite;
import com.cinema.management.entity.Seance;
import com.cinema.management.repository.DiffusionPubliciteRepository;
import com.cinema.management.repository.PaiementContratPubliciteRepository;
import com.cinema.management.repository.SeanceRepository;
import com.cinema.management.repository.TicketRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeanceRevenueService {

  private final SeanceRepository seanceRepository;
  private final TicketRepository ticketRepository;
  private final DiffusionPubliciteRepository diffusionPubliciteRepository;
  private final PaiementContratPubliciteRepository paiementContratPubliciteRepository;

  public SeanceRevenueService(
      SeanceRepository seanceRepository,
      TicketRepository ticketRepository,
      DiffusionPubliciteRepository diffusionPubliciteRepository,
      PaiementContratPubliciteRepository paiementContratPubliciteRepository) {
    this.seanceRepository = seanceRepository;
    this.ticketRepository = ticketRepository;
    this.diffusionPubliciteRepository = diffusionPubliciteRepository;
    this.paiementContratPubliciteRepository = paiementContratPubliciteRepository;
  }

  @Transactional(readOnly = true)
  public List<SeanceRevenueDto> getSeanceRevenueStats(Long filmId, String month) {
    // 1. Fetch all Seances
    List<Seance> seances = seanceRepository.findAll();

    // Filter by Film
    if (filmId != null) {
      seances =
          seances.stream()
              .filter(s -> s.getFilm() != null && s.getFilm().getId().equals(filmId))
              .collect(Collectors.toList());
    }

    // Filter by Month (YYYY-MM)
    if (month != null && !month.isEmpty()) {
      seances =
          seances.stream()
              .filter(
                  s -> {
                    LocalDate date = LocalDate.ofInstant(s.getDateHeure(), ZoneId.systemDefault());
                    String seanceMonth = date.toString().substring(0, 7);
                    return seanceMonth.equals(month);
                  })
              .collect(Collectors.toList());
    }

    // 2. Fetch Ticket Sales grouped by Seance
    // We assume "sold" means status is CONFIRMEE or PAYEE.
    List<String> soldStatuses = List.of("CONFIRMEE", "PAYEE");
    Map<Long, BigDecimal> ticketSalesMap =
        ticketRepository.sumPricesGroupBySeance(soldStatuses).stream()
            .collect(
                Collectors.toMap(
                    TicketRepository.SeanceRevenueProjection::getSeanceId,
                    TicketRepository.SeanceRevenueProjection::getTotal));

    // 3. Fetch all Diffusions grouped by Seance
    Map<Long, List<DiffusionPublicite>> diffusionsBySeance =
        diffusionPubliciteRepository.findAll().stream()
            .collect(Collectors.groupingBy(d -> d.getSeance().getId()));

    // 4. Fetch Total Payments grouped by Contract
    Map<Long, BigDecimal> paidByContratMap =
        paiementContratPubliciteRepository.sumMontantGroupByContrat().stream()
            .collect(
                Collectors.toMap(
                    PaiementContratPubliciteRepository.ContratPaymentProjection::getContratId,
                    PaiementContratPubliciteRepository.ContratPaymentProjection::getTotal));

    // 5. Build DTOs
    List<SeanceRevenueDto> results = new ArrayList<>();

    for (Seance seance : seances) {
      // Basic Info
      String filmTitre = seance.getFilm() != null ? seance.getFilm().getTitre() : "Inconnu";
      LocalDate dateDiffusion = LocalDate.ofInstant(seance.getDateHeure(), ZoneId.systemDefault());
      LocalTime heureDiffusion = LocalTime.ofInstant(seance.getDateHeure(), ZoneId.systemDefault());

      // Ticket Revenue
      BigDecimal ticketRevenue = ticketSalesMap.getOrDefault(seance.getId(), BigDecimal.ZERO);

      // Ad Revenue
      BigDecimal adRevenueTotal = BigDecimal.ZERO;
      BigDecimal adRevenueReal = BigDecimal.ZERO;

      List<DiffusionPublicite> diffusions =
          diffusionsBySeance.getOrDefault(seance.getId(), List.of());

      for (DiffusionPublicite diff : diffusions) {
        ContratPublicite contrat = diff.getContratPublicite();
        if (contrat != null && contrat.getNbDiffusions() != null && contrat.getNbDiffusions() > 0) {
          BigDecimal nbDiffusionsContrat = new BigDecimal(contrat.getNbDiffusions());
          BigDecimal nbDiffusionsSeance =
              diff.getNombrePub() != null
                  ? new BigDecimal(diff.getNombrePub())
                  : BigDecimal.ONE; // Default to 1 if null

          // Calculate Total Share
          if (contrat.getMontantTotal() != null) {
            BigDecimal share =
                contrat
                    .getMontantTotal()
                    .multiply(nbDiffusionsSeance)
                    .divide(nbDiffusionsContrat, 2, RoundingMode.HALF_UP);
            adRevenueTotal = adRevenueTotal.add(share);
          }

          // Calculate Real Share (Paid)
          BigDecimal totalPaid = paidByContratMap.getOrDefault(contrat.getId(), BigDecimal.ZERO);
          BigDecimal sharePaid =
              totalPaid
                  .multiply(nbDiffusionsSeance)
                  .divide(nbDiffusionsContrat, 2, RoundingMode.HALF_UP);
          adRevenueReal = adRevenueReal.add(sharePaid);
        }
      }

      // Aggregates
      BigDecimal chiffreAffaireTotal = ticketRevenue.add(adRevenueTotal);
      BigDecimal chiffreAffaireReel = ticketRevenue.add(adRevenueReal);

      results.add(
          new SeanceRevenueDto(
              filmTitre,
              dateDiffusion,
              heureDiffusion,
              adRevenueTotal,
              adRevenueReal,
              ticketRevenue,
              chiffreAffaireTotal,
              chiffreAffaireReel));
    }

    return results;
  }
}
