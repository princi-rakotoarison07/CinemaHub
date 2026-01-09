package com.cinema.management.service;

import com.cinema.management.entity.Test;
import com.cinema.management.repository.TestRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TestService {

  private final TestRepository testRepository;

  public TestService(TestRepository testRepository) {
    this.testRepository = testRepository;
  }

  public List<Test> findAll() {
    return testRepository.findAll();
  }

  public Test create(Test test) {
    return testRepository.save(test);
  }

  public Optional<Test> findById(Long id) {
    return testRepository.findById(id);
  }

  public Test update(Long id, Test test) {
    Test existing = testRepository.findById(id).orElseThrow();
    existing.setIp(test.getIp());
    existing.setPort(test.getPort());
    return testRepository.save(existing);
  }

  public void delete(Long id) {
    testRepository.deleteById(id);
  }
}
