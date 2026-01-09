package com.cinema.management.service;

import com.cinema.management.entity.Test;
import com.cinema.management.repository.TestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestService {

    private final TestRepository testRepository;

    public TestService(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    // Récupère toutes les entrées
    public List<Test> findAll() {
        return testRepository.findAll();
    }

    // Récupère par id
    public Optional<Test> findById(Long id) {
        return testRepository.findById(id);
    }

    // Crée une nouvelle entrée
    public Test create(Test test) {
        return testRepository.save(test);
    }

    // Met à jour une entrée
    public Test update(Long id, Test updatedTest) {
        return testRepository.findById(id)
            .map(test -> {
                test.setIp(updatedTest.getIp());
                test.setPort(updatedTest.getPort());
                return testRepository.save(test);
            })
            .orElseThrow(() -> new RuntimeException("Test not found with id " + id));
    }

    // Supprime une entrée
    public void delete(Long id) {
        testRepository.deleteById(id);
    }
}
