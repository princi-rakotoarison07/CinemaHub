package com.cinema.management.controller;

import com.cinema.management.entity.Film;
import com.cinema.management.service.FilmService;
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
@RequestMapping("/api/films")
public class FilmController {

  private final FilmService filmService;

  public FilmController(FilmService filmService) {
    this.filmService = filmService;
  }

  @GetMapping
  public List<Film> getAll() {
    return filmService.findAll();
  }

  @GetMapping("/{id}")
  public Optional<Film> getById(@PathVariable Long id) {
    return filmService.findById(id);
  }

  @PostMapping
  public Film create(@RequestBody Film film) {
    return filmService.create(film);
  }

  @PutMapping("/{id}")
  public Film update(@PathVariable Long id, @RequestBody Film film) {
    return filmService.update(id, film);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    filmService.delete(id);
  }
}
