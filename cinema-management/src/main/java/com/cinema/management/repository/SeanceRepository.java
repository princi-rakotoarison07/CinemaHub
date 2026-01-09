package com.cinema.management.repository;

import com.cinema.management.entity.Seance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeanceRepository extends JpaRepository<Seance, Long> {
}
