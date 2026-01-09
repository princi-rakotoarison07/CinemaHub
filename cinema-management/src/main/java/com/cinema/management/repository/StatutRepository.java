package com.cinema.management.repository;

import com.cinema.management.entity.Statut;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatutRepository extends JpaRepository<Statut, Long> {
  Optional<Statut> findByCode(String code);
}
