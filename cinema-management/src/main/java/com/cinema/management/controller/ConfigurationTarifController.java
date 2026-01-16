package com.cinema.management.controller;

import com.cinema.management.entity.ConfigurationTarif;
import com.cinema.management.service.ConfigurationTarifService;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/configuration-tarifs")
public class ConfigurationTarifController {

  private final ConfigurationTarifService configurationTarifService;

  public ConfigurationTarifController(ConfigurationTarifService configurationTarifService) {
    this.configurationTarifService = configurationTarifService;
  }

  @GetMapping
  public List<ConfigurationTarif> getAll() {
    return configurationTarifService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<ConfigurationTarif> getById(@PathVariable Long id) {
    return configurationTarifService.findById(id);
  }

  @PostMapping
  public ConfigurationTarif create(@RequestBody ConfigurationTarif cfg) {
    return configurationTarifService.create(cfg);
  }

  @PutMapping("/{id}")
  public ConfigurationTarif update(@PathVariable Long id, @RequestBody ConfigurationTarif cfg) {
    return configurationTarifService.update(id, cfg);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    configurationTarifService.delete(id);
  }
}
