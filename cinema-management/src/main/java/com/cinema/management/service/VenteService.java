package com.cinema.management.service;

import com.cinema.management.entity.Client;
import com.cinema.management.entity.ProduitTarif;
import com.cinema.management.entity.Vente;
import com.cinema.management.entity.VenteLigne;
import com.cinema.management.repository.ClientRepository;
import com.cinema.management.repository.ProduitExtraRepository;
import com.cinema.management.repository.VenteLigneRepository;
import com.cinema.management.repository.VenteRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class VenteService {

  private final VenteRepository venteRepository;
  private final VenteLigneRepository venteLigneRepository;
  private final ProduitExtraRepository produitExtraRepository;
  private final ClientRepository clientRepository;
  private final ProduitTarifService produitTarifService;

  public VenteService(
      VenteRepository venteRepository,
      VenteLigneRepository venteLigneRepository,
      ProduitExtraRepository produitExtraRepository,
      ClientRepository clientRepository,
      ProduitTarifService produitTarifService) {
    this.venteRepository = venteRepository;
    this.venteLigneRepository = venteLigneRepository;
    this.produitExtraRepository = produitExtraRepository;
    this.clientRepository = clientRepository;
    this.produitTarifService = produitTarifService;
  }

  public List<Vente> findAll() {
    return venteRepository.findAll();
  }

  public void delete(Long id) {
    venteRepository.deleteById(id);
  }

  public Vente create(CreateVenteRequest req) {
    if (req == null) throw new IllegalArgumentException("Requête obligatoire");

    LocalDate d = req.dateVente() != null ? req.dateVente() : LocalDate.now();
    Instant dateVente = d.atStartOfDay(ZoneId.systemDefault()).toInstant();

    Client client = null;
    if (req.idClient() != null) {
      client = clientRepository.findById(req.idClient()).orElseThrow();
    }

    Vente vente = new Vente();
    vente.setClient(client);
    vente.setDateVente(dateVente);
    vente.setMontantTotal(BigDecimal.ZERO);
    Vente saved = venteRepository.save(vente);

    BigDecimal total = BigDecimal.ZERO;

    if (req.lignes() != null) {
      for (CreateVenteLigneRequest l : req.lignes()) {
        if (l == null) continue;
        if (l.idProduitExtra() == null) continue;
        if (l.quantite() == null || l.quantite() <= 0) {
          throw new IllegalArgumentException("Quantité invalide");
        }

        var produit = produitExtraRepository.findById(l.idProduitExtra()).orElseThrow();
        ProduitTarif tarif = produitTarifService.findApplicableOrThrow(produit.getId(), d);

        BigDecimal prixUnitaire = tarif.getPrix() == null ? BigDecimal.ZERO : tarif.getPrix();
        BigDecimal montantLigne = prixUnitaire.multiply(BigDecimal.valueOf(l.quantite()));

        VenteLigne vl = new VenteLigne();
        vl.setVente(saved);
        vl.setProduitExtra(produit);
        vl.setQuantite(l.quantite());
        vl.setPrixUnitaire(prixUnitaire);
        venteLigneRepository.save(vl);

        total = total.add(montantLigne);
      }
    }

    saved.setMontantTotal(total);
    return venteRepository.save(saved);
  }

  public List<VenteLigne> findLignes(Long venteId) {
    return venteLigneRepository.findByVenteId(venteId);
  }

  public VenteDetails getDetails(Long venteId) {
    if (venteId == null) throw new IllegalArgumentException("Vente obligatoire");
    Vente vente = venteRepository.findById(venteId).orElseThrow();
    List<VenteLigne> lignes = venteLigneRepository.findByVenteId(venteId);
    BigDecimal total = vente.getMontantTotal() != null ? vente.getMontantTotal() : BigDecimal.ZERO;
    return new VenteDetails(vente.getId(), total, lignes);
  }

  @Transactional
  public VenteDetails updateLigneQuantite(Long ligneId, Integer quantite) {
    if (ligneId == null) throw new IllegalArgumentException("Ligne obligatoire");
    if (quantite == null || quantite <= 0) throw new IllegalArgumentException("Quantité invalide");

    VenteLigne ligne = venteLigneRepository.findById(ligneId).orElseThrow();
    if (ligne.getVente() == null || ligne.getVente().getId() == null) {
      throw new IllegalArgumentException("Vente introuvable pour la ligne");
    }

    ligne.setQuantite(quantite);
    venteLigneRepository.save(ligne);

    return recomputeTotalForVente(ligne.getVente().getId());
  }

  @Transactional
  public VenteDetails deleteLigne(Long ligneId) {
    if (ligneId == null) throw new IllegalArgumentException("Ligne obligatoire");
    VenteLigne ligne = venteLigneRepository.findById(ligneId).orElseThrow();
    if (ligne.getVente() == null || ligne.getVente().getId() == null) {
      throw new IllegalArgumentException("Vente introuvable pour la ligne");
    }

    Long venteId = ligne.getVente().getId();
    venteLigneRepository.delete(ligne);
    return recomputeTotalForVente(venteId);
  }

  private VenteDetails recomputeTotalForVente(Long venteId) {
    Vente vente = venteRepository.findById(venteId).orElseThrow();
    List<VenteLigne> lignes = venteLigneRepository.findByVenteId(venteId);

    BigDecimal total = BigDecimal.ZERO;
    for (VenteLigne l : lignes) {
      if (l == null) continue;
      BigDecimal pu = l.getPrixUnitaire() != null ? l.getPrixUnitaire() : BigDecimal.ZERO;
      Integer q = l.getQuantite() != null ? l.getQuantite() : 0;
      if (q <= 0) continue;
      total = total.add(pu.multiply(BigDecimal.valueOf(q)));
    }

    vente.setMontantTotal(total);
    Vente saved = venteRepository.save(vente);
    return new VenteDetails(saved.getId(), saved.getMontantTotal(), lignes);
  }

  public record CreateVenteRequest(Long idClient, LocalDate dateVente, List<CreateVenteLigneRequest> lignes) {}

  public record CreateVenteLigneRequest(Long idProduitExtra, Integer quantite) {}

  public record UpdateVenteLigneRequest(Integer quantite) {}

  public record VenteDetails(Long id, BigDecimal montantTotal, List<VenteLigne> lignes) {}
}
