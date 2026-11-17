package hai913i.tp3.parc.depot.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hai913i.tp3.parc.depot.ParcDepot;
import hai913i.tp3.parc.domaine.EtatVelo;
import hai913i.tp3.parc.domaine.Station;
import hai913i.tp3.parc.domaine.Velo;

/**
 * Le stockage, en base.
 *
 * <p>Le seul endroit du service qui connaît JPA.
 * Il traduit les entités en objets du métier et n'expose aucune entité dehors.</p>
 */
@Repository
public class ParcDepotJpa implements ParcDepot {

    private final StationJpa stations;
    private final VeloJpa velos;

    ParcDepotJpa(StationJpa stations, VeloJpa velos) {
        this.stations = stations;
        this.velos = velos;
    }

    @Override
    public List<Station> stations() {
        return stations.findAllByOrderByCodeAsc().stream().map(ParcDepotJpa::station).toList();
    }

    @Override
    public Optional<Station> parCode(String code) {
        return stations.findById(code).map(ParcDepotJpa::station);
    }

    @Override
    public List<Velo> velosDe(String code) {
        return velos.findByCodeStationOrderByIdentifiantAsc(code).stream()
                .map(ParcDepotJpa::velo).toList();
    }

    @Override
    public int disponibles(String code) {
        return velos.countByCodeStationAndEtat(code, EtatVelo.DISPONIBLE);
    }

    @Override
    public int presents(String code) {
        return velos.countByCodeStation(code);
    }

    @Override
    public Optional<Velo> premierDisponible(String code) {
        return velos.findByCodeStationAndEtatOrderByIdentifiantAsc(code, EtatVelo.DISPONIBLE)
                .stream().findFirst().map(ParcDepotJpa::velo);
    }

    @Override
    public Optional<Velo> velo(String identifiant) {
        return velos.findById(identifiant).map(ParcDepotJpa::velo);
    }

    @Override
    @Transactional
    public void retirer(String identifiant) {
        velos.findById(identifiant).ifPresent(e -> e.placer(null, EtatVelo.EN_COURSE));
    }

    @Override
    @Transactional
    public void deposer(String identifiant, String code) {
        velos.findById(identifiant).ifPresent(e -> e.placer(code, EtatVelo.DISPONIBLE));
    }

    @Override
    public int totalVelos() {
        return (int) velos.count();
    }

    private static Station station(StationEntite e) {
        return new Station(e.getCode(), e.getNom(), e.getCapacite(), e.isHorsService());
    }

    private static Velo velo(VeloEntite e) {
        return new Velo(e.getIdentifiant(), e.getCodeStation(), e.getEtat());
    }
}
