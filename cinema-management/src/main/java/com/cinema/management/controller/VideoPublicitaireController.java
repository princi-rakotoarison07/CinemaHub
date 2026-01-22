package com.cinema.management.controller;

import com.cinema.management.entity.VideoPublicitaire;
import com.cinema.management.service.VideoPublicitaireService;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/videos-publicitaires")
public class VideoPublicitaireController {

  private final VideoPublicitaireService service;

  public VideoPublicitaireController(VideoPublicitaireService service) {
    this.service = service;
  }

  @GetMapping
  public List<VideoPublicitaire> getAll() {
    return service.findAll();
  }

  @GetMapping("/{id}")
  public Optional<VideoPublicitaire> getById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public VideoPublicitaire create(@RequestBody VideoPublicitaire video) {
    try {
      return service.create(video);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PutMapping("/{id}")
  public VideoPublicitaire update(@PathVariable Long id, @RequestBody VideoPublicitaire video) {
    try {
      return service.update(id, video);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    try {
      service.delete(id);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Impossible de supprimer: la vidéo est liée à des contrats",
          e);
    }
  }
}
