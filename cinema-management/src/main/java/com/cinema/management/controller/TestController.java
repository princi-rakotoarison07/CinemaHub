package com.cinema.management.controller;

import com.cinema.management.entity.Test;
import com.cinema.management.service.TestService;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "http://localhost:5173")public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping
    public List<Test> getAll() {
        return testService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Test> getById(@PathVariable Long id) {
        return testService.findById(id);
    }

    @PostMapping
    public Test create(@RequestBody Test test) {
        return testService.create(test);
    }

    @PutMapping("/{id}")
    public Test update(@PathVariable Long id, @RequestBody Test test) {
        return testService.update(id, test);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        testService.delete(id);
    }
}
