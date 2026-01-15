package com.cinema.management.service;

import com.cinema.management.entity.Place;
import com.cinema.management.entity.Seance;
import com.cinema.management.entity.SeancePlace;
import com.cinema.management.entity.SeancePlaceId;
import com.cinema.management.entity.TypePlace;
import com.cinema.management.repository.SeancePlaceRepository;
import com.cinema.management.repository.TypePlaceRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeancePlaceService {

  private final SeancePlaceRepository seancePlaceRepository;
  private final TypePlaceRepository typePlaceRepository;

  public SeancePlaceService(
      SeancePlaceRepository seancePlaceRepository, TypePlaceRepository typePlaceRepository) {
    this.seancePlaceRepository = seancePlaceRepository;
    this.typePlaceRepository = typePlaceRepository;
  }

  public List<SeancePlace> findBySeanceId(Long seanceId) {
    return seancePlaceRepository.findBySeanceId(seanceId);
  }

  @Transactional
  public void initFromPlaces(Seance seance, List<Place> places) {
    if (seance == null || seance.getId() == null) {
      throw new IllegalArgumentException("Séance invalide");
    }
    if (places == null || places.isEmpty()) {
      return;
    }

    List<SeancePlace> rows = new ArrayList<>(places.size());
    for (Place p : places) {
      if (p.getId() == null || p.getTypePlace() == null || p.getTypePlace().getId() == null) {
        continue;
      }
      SeancePlace sp = new SeancePlace();
      sp.setId(new SeancePlaceId(seance.getId(), p.getId()));
      sp.setSeance(seance);
      sp.setPlace(p);
      sp.setTypePlace(p.getTypePlace());
      rows.add(sp);
    }

    seancePlaceRepository.saveAll(rows);
  }

  @Transactional
  public List<SeancePlace> updateTypes(Long seanceId, Collection<UpdateItem> items) {
    if (items == null || items.isEmpty()) {
      return List.of();
    }

    Set<Long> typeIds =
        items.stream()
            .map(UpdateItem::typePlaceId)
            .filter(v -> v != null)
            .collect(Collectors.toSet());

    Map<Long, TypePlace> typeById = new HashMap<>();
    if (!typeIds.isEmpty()) {
      typePlaceRepository.findAllById(typeIds).forEach(tp -> typeById.put(tp.getId(), tp));
    }

    List<SeancePlace> updated = new ArrayList<>();
    for (UpdateItem item : items) {
      if (item == null || item.placeId() == null || item.typePlaceId() == null) {
        continue;
      }
      TypePlace tp = typeById.get(item.typePlaceId());
      if (tp == null) {
        throw new IllegalStateException("Type de place introuvable: " + item.typePlaceId());
      }

      SeancePlace sp =
          seancePlaceRepository
              .findByIdSeanceIdAndIdPlaceId(seanceId, item.placeId())
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Configuration de place introuvable pour séance="
                              + seanceId
                              + ", place="
                              + item.placeId()));

      sp.setTypePlace(tp);
      updated.add(sp);
    }

    return seancePlaceRepository.saveAll(updated);
  }

  public record UpdateItem(Long placeId, Long typePlaceId) {}
}
