package com.cinema.management.service;

import com.cinema.management.entity.Societe;
import com.cinema.management.repository.SocieteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SocieteService {

  private final SocieteRepository societeRepository;

  public SocieteService(SocieteRepository societeRepository) {
    this.societeRepository = societeRepository;
  }

  public List<Societe> findAll() {
    return societeRepository.findAll();
  }

  public Optional<Societe> findById(Long id) {
    return societeRepository.findById(id);
  }

  public Societe create(Societe societe) {
    return societeRepository.save(societe);
  }

  public Societe update(Long id, Societe societe) {
    Societe existing = societeRepository.findById(id).orElseThrow();
    existing.setNom(societe.getNom());
    existing.setContact(societe.getContact());
    existing.setEmail(societe.getEmail());
    existing.setTelephone(societe.getTelephone());
    existing.setActif(societe.getActif());
    return societeRepository.save(existing);
  }

  public void delete(Long id) {
    societeRepository.deleteById(id);
  }
}
