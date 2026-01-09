package com.cinema.management.service;

import com.cinema.management.entity.Client;
import com.cinema.management.repository.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

  private final ClientRepository clientRepository;

  public ClientService(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  public List<Client> findAll() {
    return clientRepository.findAll();
  }

  public Optional<Client> findById(Long id) {
    return clientRepository.findById(id);
  }

  public Client create(Client client) {
    return clientRepository.save(client);
  }

  public Client update(Long id, Client client) {
    Client existing = clientRepository.findById(id).orElseThrow();
    existing.setNom(client.getNom());
    existing.setPrenom(client.getPrenom());
    existing.setEmail(client.getEmail());
    existing.setTelephone(client.getTelephone());
    return clientRepository.save(existing);
  }

  public void delete(Long id) {
    clientRepository.deleteById(id);
  }
}
