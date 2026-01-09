package com.cinema.management.controller;

import com.cinema.management.entity.Client;
import com.cinema.management.service.ClientService;
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
@RequestMapping("/api/clients")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @GetMapping
  public List<Client> getAll() {
    return clientService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Client> getById(@PathVariable Long id) {
    return clientService.findById(id);
  }

  @PostMapping
  public Client create(@RequestBody Client client) {
    return clientService.create(client);
  }

  @PutMapping("/{id}")
  public Client update(@PathVariable Long id, @RequestBody Client client) {
    return clientService.update(id, client);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    clientService.delete(id);
  }
}
