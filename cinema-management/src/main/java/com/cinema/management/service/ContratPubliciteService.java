package com.cinema.management.service;

import com.cinema.management.dto.CreateContratPubliciteRequest;
import com.cinema.management.entity.ContratPublicite;
import com.cinema.management.entity.DiffusionPublicite;
import com.cinema.management.entity.Seance;
import com.cinema.management.entity.TarifPublicite;
import com.cinema.management.entity.VideoPublicitaire;
import com.cinema.management.repository.ContratPubliciteRepository;
import com.cinema.management.repository.DiffusionPubliciteRepository;
import com.cinema.management.repository.SeanceRepository;
import com.cinema.management.repository.TarifPubliciteRepository;
import com.cinema.management.repository.VideoPublicitaireRepository;
import java.math.BigDecimal;
import java.util.List;
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

  public ContratPubliciteService(
      ContratPubliciteRepository contratPubliciteRepository,
      TarifPubliciteRepository tarifPubliciteRepository,
      VideoPublicitaireRepository videoPublicitaireRepository,
      SeanceRepository seanceRepository,
      DiffusionPubliciteRepository diffusionPubliciteRepository) {
    this.contratPubliciteRepository = contratPubliciteRepository;
    this.tarifPubliciteRepository = tarifPubliciteRepository;
    this.videoPublicitaireRepository = videoPublicitaireRepository;
    this.seanceRepository = seanceRepository;
    this.diffusionPubliciteRepository = diffusionPubliciteRepository;
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
}
