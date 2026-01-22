package com.cinema.management.repository;

import com.cinema.management.entity.ContratPublicite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratPubliciteRepository extends JpaRepository<ContratPublicite, Long> {

  @EntityGraph(attributePaths = {"societe", "tarifPublicite"})
  List<ContratPublicite> findAll();

  @EntityGraph(attributePaths = {"societe", "tarifPublicite"})
  Optional<ContratPublicite> findById(Long id);
}
