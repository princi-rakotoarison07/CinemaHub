package com.cinema.management.service;

import com.cinema.management.entity.Societe;
import com.cinema.management.entity.VideoPublicitaire;
import com.cinema.management.repository.SocieteRepository;
import com.cinema.management.repository.VideoPublicitaireRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class VideoPublicitaireService {

  private final VideoPublicitaireRepository repository;
  private final SocieteRepository societeRepository;

  public VideoPublicitaireService(
      VideoPublicitaireRepository repository, SocieteRepository societeRepository) {
    this.repository = repository;
    this.societeRepository = societeRepository;
  }

  public List<VideoPublicitaire> findAll() {
    return repository.findAll();
  }

  public Optional<VideoPublicitaire> findById(Long id) {
    return repository.findById(id);
  }

  public VideoPublicitaire create(VideoPublicitaire video) {
    if (video == null) throw new IllegalArgumentException("Vidéo obligatoire");
    if (video.getSociete() == null || video.getSociete().getId() == null) {
      throw new IllegalArgumentException("Société obligatoire");
    }
    if (video.getTitre() == null || video.getTitre().trim().isEmpty()) {
      throw new IllegalArgumentException("Titre obligatoire");
    }
    if (video.getDureeSecondes() == null || video.getDureeSecondes() <= 0) {
      throw new IllegalArgumentException("Durée invalide");
    }

    Societe societe = societeRepository.findById(video.getSociete().getId()).orElseThrow();
    video.setSociete(societe);
    return repository.save(video);
  }

  public VideoPublicitaire update(Long id, VideoPublicitaire video) {
    VideoPublicitaire existing = repository.findById(id).orElseThrow();

    if (video == null) throw new IllegalArgumentException("Vidéo obligatoire");
    if (video.getSociete() == null || video.getSociete().getId() == null) {
      throw new IllegalArgumentException("Société obligatoire");
    }
    if (video.getTitre() == null || video.getTitre().trim().isEmpty()) {
      throw new IllegalArgumentException("Titre obligatoire");
    }
    if (video.getDureeSecondes() == null || video.getDureeSecondes() <= 0) {
      throw new IllegalArgumentException("Durée invalide");
    }

    Societe societe = societeRepository.findById(video.getSociete().getId()).orElseThrow();
    existing.setSociete(societe);
    existing.setTitre(video.getTitre());
    existing.setDureeSecondes(video.getDureeSecondes());
    existing.setDateCreation(video.getDateCreation());
    return repository.save(existing);
  }

  public void delete(Long id) {
    repository.deleteById(id);
  }
}
