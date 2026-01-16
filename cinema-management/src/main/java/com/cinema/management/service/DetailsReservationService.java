package com.cinema.management.service;

import com.cinema.management.entity.DetailsReservation;
import com.cinema.management.repository.DetailsReservationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DetailsReservationService {

  private final DetailsReservationRepository detailsReservationRepository;

  public DetailsReservationService(DetailsReservationRepository detailsReservationRepository) {
    this.detailsReservationRepository = detailsReservationRepository;
  }

  public List<DetailsReservation> findAll() {
    return detailsReservationRepository.findAll();
  }

  public Optional<DetailsReservation> findById(Long id) {
    return detailsReservationRepository.findById(id);
  }

  public List<DetailsReservation> findByReservationId(Long reservationId) {
    return detailsReservationRepository.findByReservationId(reservationId);
  }
}
