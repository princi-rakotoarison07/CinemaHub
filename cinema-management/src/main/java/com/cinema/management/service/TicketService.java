package com.cinema.management.service;

import com.cinema.management.entity.Ticket;
import com.cinema.management.repository.TicketRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

  private final TicketRepository ticketRepository;

  public TicketService(TicketRepository ticketRepository) {
    this.ticketRepository = ticketRepository;
  }

  public List<Ticket> findAll() {
    return ticketRepository.findAll();
  }

  public Optional<Ticket> findById(Long id) {
    return ticketRepository.findById(id);
  }

  public List<Ticket> findByReservationId(Long reservationId) {
    return ticketRepository.findByReservationId(reservationId);
  }

  public Ticket create(Ticket ticket) {
    return ticketRepository.save(ticket);
  }

  public Ticket update(Long id, Ticket ticket) {
    Ticket existing = ticketRepository.findById(id).orElseThrow();
    existing.setReservation(ticket.getReservation());
    existing.setPlace(ticket.getPlace());
    existing.setCategorieClient(ticket.getCategorieClient());
    existing.setPrix(ticket.getPrix());
    return ticketRepository.save(existing);
  }

  public void delete(Long id) {
    ticketRepository.deleteById(id);
  }
}
