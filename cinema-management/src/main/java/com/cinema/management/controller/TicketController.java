package com.cinema.management.controller;

import com.cinema.management.entity.Ticket;
import com.cinema.management.service.TicketService;
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
@RequestMapping("/api/tickets")
public class TicketController {

  private final TicketService ticketService;

  public TicketController(TicketService ticketService) {
    this.ticketService = ticketService;
  }

  @GetMapping
  public List<Ticket> getAll() {
    return ticketService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Ticket> getById(@PathVariable Long id) {
    return ticketService.findById(id);
  }

  @PostMapping
  public Ticket create(@RequestBody Ticket ticket) {
    return ticketService.create(ticket);
  }

  @PutMapping("/{id}")
  public Ticket update(@PathVariable Long id, @RequestBody Ticket ticket) {
    return ticketService.update(id, ticket);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    ticketService.delete(id);
  }
}
