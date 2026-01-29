package com.cinema.management.service;

import com.cinema.management.entity.DetailsReservation;
import com.cinema.management.entity.DiffusionPublicite;
import com.cinema.management.entity.Seance;
import com.cinema.management.entity.Tarif;
import com.cinema.management.repository.ConfigurationTarifRepository;
import com.cinema.management.repository.DetailsReservationRepository;
import com.cinema.management.repository.DiffusionPubliciteRepository;
import com.cinema.management.repository.PaiementContratPubliciteRepository;
import com.cinema.management.repository.SeanceRepository;
import com.cinema.management.repository.TarifRepository;
import java.util.Collection;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SeanceChiffreAffaireService {

  private final SeanceRepository seanceRepository;
  private final DetailsReservationRepository detailsReservationRepository;
  private final TarifRepository tarifRepository;
  private final ConfigurationTarifRepository configurationTarifRepository;
  private final DiffusionPubliciteRepository diffusionPubliciteRepository;
  private final PaiementContratPubliciteRepository paiementContratPubliciteRepository;

  public SeanceChiffreAffaireService(
      SeanceRepository seanceRepository,
      DetailsReservationRepository detailsReservationRepository,
      TarifRepository tarifRepository,
      ConfigurationTarifRepository configurationTarifRepository,
      DiffusionPubliciteRepository diffusionPubliciteRepository,
      PaiementContratPubliciteRepository paiementContratPubliciteRepository) {
    this.seanceRepository = seanceRepository;
    this.detailsReservationRepository = detailsReservationRepository;
    this.tarifRepository = tarifRepository;
    this.configurationTarifRepository = configurationTarifRepository;
    this.diffusionPubliciteRepository = diffusionPubliciteRepository;
    this.paiementContratPubliciteRepository = paiementContratPubliciteRepository;
  }

  public List<SeanceChiffreAffaireDto> getChiffreAffaireBySeance(LocalDate dateDebut, LocalDate dateFin) {
    List<Seance> seances = findSeances(dateDebut, dateFin);

    List<Long> seanceIds = seances.stream().map(Seance::getId).toList();
    if (seanceIds.isEmpty()) return List.of();

    Map<Long, BigDecimal> ticketsBySeance = computeTicketRevenueBySeance(seanceIds);
    Map<Long, BigDecimal> pubBySeance = computePubliciteRevenueBySeance(seanceIds);
    PubliciteBreakdownBySeance pubsPaidAndReste = computePublicitePaidAndResteBySeance(seanceIds);

    List<SeanceChiffreAffaireDto> out = new ArrayList<>();
    for (Seance s : seances) {
      Long id = s.getId();
      BigDecimal tickets = ticketsBySeance.getOrDefault(id, BigDecimal.ZERO);
      BigDecimal pub = pubBySeance.getOrDefault(id, BigDecimal.ZERO);
      BigDecimal pubPaye = pubsPaidAndReste.payeBySeance().getOrDefault(id, BigDecimal.ZERO);
      BigDecimal pubReste = pubsPaidAndReste.resteBySeance().getOrDefault(id, BigDecimal.ZERO);
      BigDecimal total = tickets.add(pub);

      String filmTitre = (s.getFilm() != null ? s.getFilm().getTitre() : null);
      out.add(new SeanceChiffreAffaireDto(id, filmTitre, s.getDateHeure(), tickets, pub, pubPaye, pubReste, total));
    }

    return out;
  }

  public SeanceChiffreAffaireDetailsDto getChiffreAffaireDetailsBySeance(Long seanceId) {
    if (seanceId == null) throw new IllegalArgumentException("Séance obligatoire");

    BigDecimal montantTickets =
        computeTicketRevenueBySeance(List.of(seanceId)).getOrDefault(seanceId, BigDecimal.ZERO);

    List<DiffusionPublicite> diffusions = diffusionPubliciteRepository.findBySeanceId(seanceId);
    Set<Long> contratIds =
        diffusions.stream()
            .map(dp -> dp != null && dp.getContratPublicite() != null ? dp.getContratPublicite().getId() : null)
            .filter(x -> x != null)
            .collect(Collectors.toSet());

    Map<Long, BigDecimal> payeParContrat = new HashMap<>();
    if (!contratIds.isEmpty()) {
      for (Object[] row : paiementContratPubliciteRepository.sumByContratIds(contratIds)) {
        if (row == null || row.length < 2) continue;
        Long cid = (Long) row[0];
        BigDecimal sum = (BigDecimal) row[1];
        payeParContrat.put(cid, sum != null ? sum : BigDecimal.ZERO);
      }
    }

    List<DiffusionPubliciteDetailDto> pubs = new ArrayList<>();
    for (DiffusionPublicite dp : diffusions) {
      if (dp == null || dp.getContratPublicite() == null) continue;
      Long contratId = dp.getContratPublicite().getId();

      String societe =
          dp.getContratPublicite().getVideoPublicitaire() != null
                  && dp.getContratPublicite().getVideoPublicitaire().getSociete() != null
              ? dp.getContratPublicite().getVideoPublicitaire().getSociete().getNom()
              : null;
      String video =
          dp.getContratPublicite().getVideoPublicitaire() != null
              ? dp.getContratPublicite().getVideoPublicitaire().getTitre()
              : null;

      Integer nombrePub = dp.getNombrePub() != null ? dp.getNombrePub() : 0;

      BigDecimal prixParDiffusion =
          dp.getContratPublicite().getTarifPublicite() != null
                  && dp.getContratPublicite().getTarifPublicite().getPrixParDiffusion() != null
              ? dp.getContratPublicite().getTarifPublicite().getPrixParDiffusion()
              : BigDecimal.ZERO;

      BigDecimal montantDiffusion = prixParDiffusion.multiply(BigDecimal.valueOf(nombrePub));

      BigDecimal montantContrat =
          dp.getContratPublicite().getMontantTotal() != null
              ? dp.getContratPublicite().getMontantTotal()
              : BigDecimal.ZERO;
      BigDecimal payeContrat = payeParContrat.getOrDefault(contratId, BigDecimal.ZERO);
      BigDecimal resteContrat = montantContrat.subtract(payeContrat);

      pubs.add(
          new DiffusionPubliciteDetailDto(
              contratId,
              societe,
              video,
              nombrePub,
              prixParDiffusion,
              montantDiffusion,
              montantContrat,
              payeContrat,
              resteContrat));
    }

    return new SeanceChiffreAffaireDetailsDto(seanceId, montantTickets, pubs);
  }

  private List<Seance> findSeances(LocalDate dateDebut, LocalDate dateFin) {
    if (dateDebut != null && dateFin != null) {
      Instant from = dateDebut.atStartOfDay().toInstant(ZoneOffset.UTC);
      Instant to = dateFin.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
      return seanceRepository.findByDateHeureBetween(from, to);
    }
    return seanceRepository.findAll();
  }

  private Map<Long, BigDecimal> computePubliciteRevenueBySeance(List<Long> seanceIds) {
    Map<Long, BigDecimal> map = new HashMap<>();
    List<Object[]> rows = diffusionPubliciteRepository.sumPubliciteBySeanceIds(seanceIds);
    for (Object[] row : rows) {
      if (row == null || row.length < 2) continue;
      Long seanceId = (Long) row[0];
      BigDecimal sum = (BigDecimal) row[1];
      map.put(seanceId, sum != null ? sum : BigDecimal.ZERO);
    }
    return map;
  }

  private PubliciteBreakdownBySeance computePublicitePaidAndResteBySeance(List<Long> seanceIds) {
    Map<Long, BigDecimal> payeBySeance = new HashMap<>();
    Map<Long, BigDecimal> resteBySeance = new HashMap<>();

    if (seanceIds == null || seanceIds.isEmpty()) {
      return new PubliciteBreakdownBySeance(payeBySeance, resteBySeance);
    }

    List<DiffusionPublicite> diffusions = diffusionPubliciteRepository.findBySeanceIdIn(seanceIds);
    if (diffusions == null || diffusions.isEmpty()) {
      return new PubliciteBreakdownBySeance(payeBySeance, resteBySeance);
    }

    Collection<Long> contratIds =
        diffusions.stream()
            .map(dp -> dp != null && dp.getContratPublicite() != null ? dp.getContratPublicite().getId() : null)
            .filter(x -> x != null)
            .collect(Collectors.toSet());

    Map<Long, BigDecimal> payeParContrat = new HashMap<>();
    if (!contratIds.isEmpty()) {
      for (Object[] row : paiementContratPubliciteRepository.sumByContratIds(contratIds)) {
        if (row == null || row.length < 2) continue;
        Long cid = (Long) row[0];
        BigDecimal sum = (BigDecimal) row[1];
        payeParContrat.put(cid, sum != null ? sum : BigDecimal.ZERO);
      }
    }

    for (DiffusionPublicite dp : diffusions) {
      if (dp == null || dp.getSeance() == null || dp.getSeance().getId() == null) continue;
      if (dp.getContratPublicite() == null || dp.getContratPublicite().getId() == null) continue;

      Long seanceId = dp.getSeance().getId();
      Long contratId = dp.getContratPublicite().getId();

      Integer nombrePub = dp.getNombrePub() != null ? dp.getNombrePub() : 0;
      BigDecimal prixParDiffusion =
          dp.getContratPublicite().getTarifPublicite() != null
                  && dp.getContratPublicite().getTarifPublicite().getPrixParDiffusion() != null
              ? dp.getContratPublicite().getTarifPublicite().getPrixParDiffusion()
              : BigDecimal.ZERO;

      BigDecimal montantDiffusion = prixParDiffusion.multiply(BigDecimal.valueOf(nombrePub));

      BigDecimal montantContrat =
          dp.getContratPublicite().getMontantTotal() != null
              ? dp.getContratPublicite().getMontantTotal()
              : BigDecimal.ZERO;
      BigDecimal payeContrat = payeParContrat.getOrDefault(contratId, BigDecimal.ZERO);

      BigDecimal ratio = BigDecimal.ZERO;
      if (montantContrat.compareTo(BigDecimal.ZERO) > 0) {
        ratio = payeContrat.divide(montantContrat, 6, RoundingMode.HALF_UP);
        if (ratio.compareTo(BigDecimal.ZERO) < 0) ratio = BigDecimal.ZERO;
        if (ratio.compareTo(BigDecimal.ONE) > 0) ratio = BigDecimal.ONE;
      }

      BigDecimal payeDiffusion = montantDiffusion.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
      BigDecimal resteDiffusion = montantDiffusion.subtract(payeDiffusion);
      if (resteDiffusion.compareTo(BigDecimal.ZERO) < 0) resteDiffusion = BigDecimal.ZERO;

      payeBySeance.put(seanceId, payeBySeance.getOrDefault(seanceId, BigDecimal.ZERO).add(payeDiffusion));
      resteBySeance.put(seanceId, resteBySeance.getOrDefault(seanceId, BigDecimal.ZERO).add(resteDiffusion));
    }

    return new PubliciteBreakdownBySeance(payeBySeance, resteBySeance);
  }

  private Map<Long, BigDecimal> computeTicketRevenueBySeance(List<Long> seanceIds) {
    Map<Long, BigDecimal> map = new HashMap<>();
    List<DetailsReservation> details =
        detailsReservationRepository.findActiveBySeanceIdsExcludingAnnulee(seanceIds);

    for (DetailsReservation d : details) {
      if (d == null) continue;
      if (d.getReservation() == null || d.getReservation().getSeance() == null) continue;
      if (d.getCategorieClient() == null || d.getCategorieClient().getId() == null) continue;
      if (d.getPlace() == null || d.getPlace().getTypePlace() == null || d.getPlace().getTypePlace().getId() == null) {
        continue;
      }

      Long seanceId = d.getReservation().getSeance().getId();
      Long typePlaceId = d.getPlace().getTypePlace().getId();
      Long categorieClientId = d.getCategorieClient().getId();

      Tarif tarif =
          tarifRepository
              .findFirstByTypePlaceIdAndCategorieClientIdAndActifTrueOrderByDateDebutDesc(
                  typePlaceId, categorieClientId)
              .orElse(null);
      if (tarif == null) continue;

      BigDecimal applied = getAppliedPrice(tarif);
      map.put(seanceId, map.getOrDefault(seanceId, BigDecimal.ZERO).add(applied));
    }

    return map;
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

  public record SeanceChiffreAffaireDto(
      Long seanceId,
      String film,
      Instant dateHeure,
      BigDecimal montantTickets,
      BigDecimal montantPublicite,
      BigDecimal montantPublicitePaye,
      BigDecimal montantPubliciteReste,
      BigDecimal montantTotal) {}

  private record PubliciteBreakdownBySeance(
      Map<Long, BigDecimal> payeBySeance,
      Map<Long, BigDecimal> resteBySeance) {}

  public record DiffusionPubliciteDetailDto(
      Long contratId,
      String societe,
      String video,
      Integer nombrePub,
      BigDecimal prixParDiffusion,
      BigDecimal montantDiffusion,
      BigDecimal montantContrat,
      BigDecimal montantPaye,
      BigDecimal montantReste) {}

  public record SeanceChiffreAffaireDetailsDto(
      Long seanceId, BigDecimal montantTickets, List<DiffusionPubliciteDetailDto> diffusionsPublicite) {}
}
