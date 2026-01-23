package com.cinema.management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record SeanceRevenueDto(
    String filmTitre,
    LocalDate dateDiffusion,
    LocalTime heureDiffusion,
    BigDecimal montantTotalPublicite,
    BigDecimal montantReelPublicite,
    BigDecimal montantTickets,
    BigDecimal chiffreAffaireTotal,
    BigDecimal chiffreAffaireReel
) {}
