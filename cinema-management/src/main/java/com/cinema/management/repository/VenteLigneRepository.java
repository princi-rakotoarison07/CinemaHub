package com.cinema.management.repository;

import com.cinema.management.entity.VenteLigne;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenteLigneRepository extends JpaRepository<VenteLigne, Long> {

  List<VenteLigne> findByVenteId(Long venteId);
}
