package com.cinema.management.service;

import com.cinema.management.dto.CreateContratPubliciteRequest;
import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.entity.DiffusionPublicite;
import com.cinema.management.repository.PaiementContratPubliciteRepository;
import com.cinema.management.entity.Seance;
import com.cinema.management.entity.TarifPublicite;
import com.cinema.management.entity.VideoPublicitaire;
import com.cinema.management.repository.ContratPubliciteRepository;
import com.cinema.management.repository.DiffusionPubliciteRepository;
import com.cinema.management.repository.SeanceRepository;
import com.cinema.management.repository.TarifPubliciteRepository;
import com.cinema.management.repository.VideoPublicitaireRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ContratPubliciteService {

  private final ContratPubliciteRepository contratPubliciteRepository;

  private final TarifPubliciteRepository tarifPubliciteRepository;
  private final VideoPublicitaireRepository videoPublicitaireRepository;
  private final SeanceRepository seanceRepository;
  private final DiffusionPubliciteRepository diffusionPubliciteRepository;
  private final PaiementContratPubliciteRepository paiementContratPubliciteRepository;

  public ContratPubliciteService(
      ContratPubliciteRepository contratPubliciteRepository,
      TarifPubliciteRepository tarifPubliciteRepository,
      VideoPublicitaireRepository videoPublicitaireRepository,
      SeanceRepository seanceRepository,
      DiffusionPubliciteRepository diffusionPubliciteRepository,
      PaiementContratPubliciteRepository paiementContratPubliciteRepository) {
    this.contratPubliciteRepository = contratPubliciteRepository;
    this.tarifPubliciteRepository = tarifPubliciteRepository;
    this.videoPublicitaireRepository = videoPublicitaireRepository;
    this.seanceRepository = seanceRepository;
    this.diffusionPubliciteRepository = diffusionPubliciteRepository;
    this.paiementContratPubliciteRepository = paiementContratPubliciteRepository;
  }

  public List<ContratPublicite> findAll() {
    return contratPubliciteRepository.findAll();
  }

  public Optional<ContratPublicite> findById(Long id) {
    return contratPubliciteRepository.findById(id);
  }

  public ContratPublicite create(ContratPublicite contrat) {
    return contratPubliciteRepository.save(contrat);
  }

  public ContratPublicite update(Long id, ContratPublicite contrat) {
    ContratPublicite existing = contratPubliciteRepository.findById(id).orElseThrow();
    existing.setVideoPublicitaire(contrat.getVideoPublicitaire());
    existing.setTarifPublicite(contrat.getTarifPublicite());
    existing.setNbDiffusions(contrat.getNbDiffusions());
    existing.setMontantTotal(contrat.getMontantTotal());
    existing.setDateDebut(contrat.getDateDebut());
    existing.setDateFin(contrat.getDateFin());
    return contratPubliciteRepository.save(existing);
  }

  public ContratPublicite createWithDiffusions(CreateContratPubliciteRequest req) {
    if (req == null) throw new IllegalArgumentException("Requête obligatoire");
    if (req.getVideoPublicitaireId() == null) throw new IllegalArgumentException("Video obligatoire");
    if (req.getNbDiffusions() == null || req.getNbDiffusions() <= 0) {
      throw new IllegalArgumentException("Nombre diffusions invalide");
    }
    if (req.getDateDebut() == null) throw new IllegalArgumentException("Date début obligatoire");

    VideoPublicitaire video = videoPublicitaireRepository.findById(req.getVideoPublicitaireId()).orElseThrow();

    TarifPublicite tarif =
        tarifPubliciteRepository.findApplicable(req.getDateDebut(), PageRequest.of(0, 1)).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Aucun tarif applicable pour cette date"));

    BigDecimal prix = tarif.getPrixParDiffusion() == null ? BigDecimal.ZERO : tarif.getPrixParDiffusion();
    BigDecimal montantTotal = prix.multiply(BigDecimal.valueOf(req.getNbDiffusions()));

    int somme = 0;
    if (req.getDiffusions() != null) {
      for (CreateContratPubliciteRequest.DiffusionSeance d : req.getDiffusions()) {
        if (d == null) continue;
        Integer n = d.getNombrePub() == null ? 0 : d.getNombrePub();
        if (n < 0) throw new IllegalArgumentException("Nombre pub invalide");
        somme += n;
      }
    }

    if (somme != req.getNbDiffusions()) {
      throw new IllegalArgumentException("Somme des pubs par séance doit être égale au nombre de diffusions");
    }

    ContratPublicite contrat = new ContratPublicite();
    contrat.setVideoPublicitaire(video);
    contrat.setTarifPublicite(tarif);
    contrat.setNbDiffusions(req.getNbDiffusions());
    contrat.setMontantTotal(montantTotal);
    contrat.setDateDebut(req.getDateDebut());
    contrat.setDateFin(req.getDateFin());
    ContratPublicite saved = contratPubliciteRepository.save(contrat);

    if (req.getDiffusions() != null) {
      for (CreateContratPubliciteRequest.DiffusionSeance d : req.getDiffusions()) {
        if (d == null || d.getSeanceId() == null) continue;
        int n = d.getNombrePub() == null ? 0 : d.getNombrePub();
        if (n <= 0) continue;
        Seance seance = seanceRepository.findById(d.getSeanceId()).orElseThrow();

        DiffusionPublicite dp = new DiffusionPublicite();
        dp.setContratPublicite(saved);
        dp.setSeance(seance);
        dp.setNombrePub(n);
        diffusionPubliciteRepository.save(dp);
      }
    }

    return contratPubliciteRepository.findById(saved.getId()).orElse(saved);
  }

  public void delete(Long id) {
    contratPubliciteRepository.deleteById(id);
  }

  public List<ContratPubliciteFactureDto> getFacturesContrats() {
    List<ContratPublicite> contrats = contratPubliciteRepository.findAll();
    Set<Long> contratIds =
        contrats.stream().map(ContratPublicite::getId).filter(x -> x != null).collect(Collectors.toSet());

    Map<Long, BigDecimal> payeByContrat = new HashMap<>();
    if (!contratIds.isEmpty()) {
      for (Object[] row : paiementContratPubliciteRepository.sumByContratIds(contratIds)) {
        if (row == null || row.length < 2) continue;
        Long cid = (Long) row[0];
        BigDecimal sum = (BigDecimal) row[1];
        payeByContrat.put(cid, sum != null ? sum : BigDecimal.ZERO);
      }
    }

    return contrats.stream()
        .map(c -> {
          Long id = c.getId();
          BigDecimal total = c.getMontantTotal() != null ? c.getMontantTotal() : BigDecimal.ZERO;
          BigDecimal paye = payeByContrat.getOrDefault(id, BigDecimal.ZERO);
          if (paye.compareTo(BigDecimal.ZERO) < 0) paye = BigDecimal.ZERO;
          if (total.compareTo(BigDecimal.ZERO) > 0 && paye.compareTo(total) > 0) paye = total;
          BigDecimal reste = total.subtract(paye);

          BigDecimal pct = BigDecimal.ZERO;
          if (total.compareTo(BigDecimal.ZERO) > 0) {
            pct = paye.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
          }

          String societeNom =
              c.getVideoPublicitaire() != null && c.getVideoPublicitaire().getSociete() != null
                  ? c.getVideoPublicitaire().getSociete().getNom()
                  : null;
          String videoTitre = c.getVideoPublicitaire() != null ? c.getVideoPublicitaire().getTitre() : null;

          return new ContratPubliciteFactureDto(
              id,
              societeNom,
              videoTitre,
              c.getNbDiffusions() != null ? c.getNbDiffusions() : 0,
              total,
              paye,
              reste,
              pct);
        })
        .toList();
  }

  public ContratPubliciteFactureDetailsDto getFactureDetails(Long contratId) {
    if (contratId == null) throw new IllegalArgumentException("Contrat obligatoire");
    ContratPublicite contrat = contratPubliciteRepository.findById(contratId).orElseThrow();

    BigDecimal total = contrat.getMontantTotal() != null ? contrat.getMontantTotal() : BigDecimal.ZERO;
    BigDecimal paye = paiementContratPubliciteRepository.sumByContratId(contratId);
    if (paye == null) paye = BigDecimal.ZERO;
    if (paye.compareTo(BigDecimal.ZERO) < 0) paye = BigDecimal.ZERO;
    if (total.compareTo(BigDecimal.ZERO) > 0 && paye.compareTo(total) > 0) paye = total;

    BigDecimal ratio = BigDecimal.ZERO;
    if (total.compareTo(BigDecimal.ZERO) > 0) {
      ratio = paye.divide(total, 6, RoundingMode.HALF_UP);
    }

    BigDecimal prixParDiffusion =
        contrat.getTarifPublicite() != null && contrat.getTarifPublicite().getPrixParDiffusion() != null
            ? contrat.getTarifPublicite().getPrixParDiffusion()
            : BigDecimal.ZERO;

    final BigDecimal ratioFinal = ratio;
    final BigDecimal prixParDiffusionFinal = prixParDiffusion;

    List<DiffusionPublicite> diffusions =
        diffusionPubliciteRepository.findWithDetailsByContratPubliciteId(contratId);

    List<ContratDiffusionDetailDto> details =
        diffusions.stream()
            .filter(dp -> dp != null && dp.getSeance() != null)
            .map(dp -> {
              Integer n = dp.getNombrePub() != null ? dp.getNombrePub() : 0;
              BigDecimal montantDiffusion = prixParDiffusionFinal.multiply(BigDecimal.valueOf(n));
              BigDecimal montantPayeDiff =
                  montantDiffusion.multiply(ratioFinal).setScale(2, RoundingMode.HALF_UP);
              BigDecimal montantResteDiff = montantDiffusion.subtract(montantPayeDiff);

              String film =
                  dp.getSeance().getFilm() != null ? dp.getSeance().getFilm().getTitre() : null;

              BigDecimal pct = ratioFinal.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

              return new ContratDiffusionDetailDto(
                  dp.getSeance().getId(),
                  film,
                  dp.getSeance().getDateHeure(),
                  n,
                  prixParDiffusionFinal,
                  montantDiffusion,
                  pct,
                  montantPayeDiff,
                  montantResteDiff);
            })
            .toList();

    BigDecimal pctTotal = BigDecimal.ZERO;
    if (total.compareTo(BigDecimal.ZERO) > 0) {
      pctTotal = paye.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP);
    }

    String societeNom =
        contrat.getVideoPublicitaire() != null && contrat.getVideoPublicitaire().getSociete() != null
            ? contrat.getVideoPublicitaire().getSociete().getNom()
            : null;
    String videoTitre = contrat.getVideoPublicitaire() != null ? contrat.getVideoPublicitaire().getTitre() : null;

    return new ContratPubliciteFactureDetailsDto(
        contratId,
        societeNom,
        videoTitre,
        contrat.getNbDiffusions() != null ? contrat.getNbDiffusions() : 0,
        total,
        paye,
        total.subtract(paye),
        pctTotal,
        details);
  }

  public record ContratPubliciteFactureDto(
      Long contratId,
      String societe,
      String video,
      Integer nbDiffusions,
      BigDecimal montantTotal,
      BigDecimal montantPaye,
      BigDecimal montantReste,
      BigDecimal pourcentagePaye) {}

  public record ContratDiffusionDetailDto(
      Long seanceId,
      String film,
      java.time.Instant dateHeure,
      Integer nombrePub,
      BigDecimal prixParDiffusion,
      BigDecimal montantDiffusion,
      BigDecimal pourcentagePaye,
      BigDecimal montantPaye,
      BigDecimal montantReste) {}

  public record ContratPubliciteFactureDetailsDto(
      Long contratId,
      String societe,
      String video,
      Integer nbDiffusions,
      BigDecimal montantTotal,
      BigDecimal montantPaye,
      BigDecimal montantReste,
      BigDecimal pourcentagePaye,
      List<ContratDiffusionDetailDto> diffusions) {}
}
