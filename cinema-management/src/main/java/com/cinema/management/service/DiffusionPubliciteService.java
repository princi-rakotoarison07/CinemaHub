package com.cinema.management.service;

import com.cinema.management.entity.DiffusionPublicite;
import com.cinema.management.repository.DiffusionPubliciteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DiffusionPubliciteService {

  private final DiffusionPubliciteRepository repository;

  public DiffusionPubliciteService(DiffusionPubliciteRepository repository) {
    this.repository = repository;
  }

  public List<DiffusionPublicite> findByContratId(Long contratId) {
    return repository.findByContratPubliciteId(contratId);
  }
}
