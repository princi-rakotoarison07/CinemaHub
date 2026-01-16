package com.cinema.management.service;

import com.cinema.management.entity.ConfigurationTarif;
import com.cinema.management.repository.ConfigurationTarifRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationTarifService {

  private final ConfigurationTarifRepository configurationTarifRepository;

  public ConfigurationTarifService(ConfigurationTarifRepository configurationTarifRepository) {
    this.configurationTarifRepository = configurationTarifRepository;
  }

  public List<ConfigurationTarif> findAll() {
    return configurationTarifRepository.findAll();
  }

  public Optional<ConfigurationTarif> findById(Long id) {
    return configurationTarifRepository.findById(id);
  }

  public ConfigurationTarif create(ConfigurationTarif cfg) {
    return configurationTarifRepository.save(cfg);
  }

  public ConfigurationTarif update(Long id, ConfigurationTarif cfg) {
    ConfigurationTarif existing = configurationTarifRepository.findById(id).orElseThrow();
    existing.setTarif1(cfg.getTarif1());
    existing.setTarif2(cfg.getTarif2());
    existing.setPourcentage(cfg.getPourcentage());
    existing.setActif(cfg.getActif());
    return configurationTarifRepository.save(existing);
  }

  public void delete(Long id) {
    configurationTarifRepository.deleteById(id);
  }
}
