package com.cinema.management.controller;

import com.cinema.management.dto.SeanceRevenueDto;
import com.cinema.management.service.SeanceRevenueService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class SeanceRevenueController {

  private final SeanceRevenueService seanceRevenueService;

  public SeanceRevenueController(SeanceRevenueService seanceRevenueService) {
    this.seanceRevenueService = seanceRevenueService;
  }

  @GetMapping("/seance-revenues")
  public List<SeanceRevenueDto> getSeanceRevenues() {
    return seanceRevenueService.getSeanceRevenueStats();
  }
}
