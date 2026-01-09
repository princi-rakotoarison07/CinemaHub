package com.cinema.management.service;

import com.cinema.management.entity.Film;
import com.cinema.management.repository.FilmRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FilmService {

  private final FilmRepository filmRepository;

  public FilmService(FilmRepository filmRepository) {
    this.filmRepository = filmRepository;
  }

  public List<Film> findAll() {
    return filmRepository.findAll();
  }

  public Optional<Film> findById(Long id) {
    return filmRepository.findById(id);
  }

  public Film create(Film film) {
    return filmRepository.save(film);
  }

  public Film update(Long id, Film film) {
    Film existing = filmRepository.findById(id).orElseThrow();
    existing.setTitre(film.getTitre());
    existing.setDescription(film.getDescription());
    existing.setDureeMinutes(film.getDureeMinutes());
    existing.setDateSortie(film.getDateSortie());
    existing.setAgeMin(film.getAgeMin());
    existing.setLangueOriginale(film.getLangueOriginale());
    existing.setGenres(film.getGenres());
    return filmRepository.save(existing);
  }

  public void delete(Long id) {
    filmRepository.deleteById(id);
  }
}
