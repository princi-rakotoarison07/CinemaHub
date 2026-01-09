package com.cinema.management.repository;

import com.cinema.management.entity.Place;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
  List<Place> findBySalleId(Long salleId);
}
