package com.cinema.management.service;

import com.cinema.management.entity.CategorieClient;
import com.cinema.management.repository.CategorieClientRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CategorieClientService {

  private final CategorieClientRepository categorieClientRepository;

  public CategorieClientService(CategorieClientRepository categorieClientRepository) {
    this.categorieClientRepository = categorieClientRepository;
  }

  public List<CategorieClient> findAll() {
    return categorieClientRepository.findAll();
  }

  public Optional<CategorieClient> findById(Long id) {
    return categorieClientRepository.findById(id);
  }
}
