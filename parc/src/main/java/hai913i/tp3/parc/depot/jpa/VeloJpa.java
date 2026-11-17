package hai913i.tp3.parc.depot.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hai913i.tp3.parc.domaine.EtatVelo;

/** Accès Spring Data aux vélos. */
public interface VeloJpa extends JpaRepository<VeloEntite, String> {

    List<VeloEntite> findByCodeStationOrderByIdentifiantAsc(String codeStation);

    List<VeloEntite> findByCodeStationAndEtatOrderByIdentifiantAsc(
            String codeStation, EtatVelo etat);

    int countByCodeStation(String codeStation);

    int countByCodeStationAndEtat(String codeStation, EtatVelo etat);
}