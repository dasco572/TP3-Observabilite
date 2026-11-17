package hai913i.tp3.parc.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import hai913i.tp3.parc.depot.ParcDepot;
import hai913i.tp3.parc.domaine.EtatVelo;
import hai913i.tp3.parc.domaine.Station;
import hai913i.tp3.parc.domaine.Velo;

/**
 * Les règles du parc.
 *
 * <p>Cette classe ignore qu'il existe un protocole HTTP.
 * Elle rend des objets du métier ou lève une faute du métier.
 * C'est la couche API qui décide du code de statut.</p>
 */
@Service
public class ServiceParc {

    private final ParcDepot depot;
    private final int consolidationMs;

    ServiceParc(ParcDepot depot, @Value("${parc.consolidation-ms:500}") int consolidationMs) {
        this.depot = depot;
        this.consolidationMs = consolidationMs;
    }

    public List<Station> stations() {
        return depot.stations();
    }

    public Station station(String code) {
        return depot.parCode(code).orElseThrow(() -> new StationIntrouvable(code));
    }

    public int disponibles(String code) {
        return depot.disponibles(code);
    }

    /**
     * Retire un vélo d'une station.
     *
     * @return le vélo retiré
     * @throws StationIntrouvable  le code ne correspond à aucune station
     * @throws StationIndisponible la station est hors service
     * @throws MouvementImpossible la station n'a plus aucun vélo disponible
     */
    public Velo retirer(String code) {
        Station station = station(code);
        if (station.horsService()) {
            throw new StationIndisponible(code);
        }
        Velo velo = depot.premierDisponible(code)
                .orElseThrow(() -> new MouvementImpossible(
                        "aucun vélo disponible à la station " + code));
        depot.retirer(velo.identifiant());
        return new Velo(velo.identifiant(), null, EtatVelo.EN_COURSE);
    }

    /**
     * Dépose un vélo à une station.
     *
     * @return le vélo déposé
     * @throws StationIntrouvable  le code ne correspond à aucune station
     * @throws StationIndisponible la station est hors service
     * @throws MouvementImpossible le vélo est inconnu, il n'est pas en
     *                             course, ou toutes les bornes sont occupées
     */
    public Velo deposer(String code, String identifiant) {
        Station station = station(code);
        if (station.horsService()) {
            throw new StationIndisponible(code);
        }
        Velo velo = depot.velo(identifiant)
                .orElseThrow(() -> new MouvementImpossible("vélo inconnu : " + identifiant));
        if (velo.etat() != EtatVelo.EN_COURSE) {
            throw new MouvementImpossible("le vélo " + identifiant + " n'est pas en course");
        }
        if (depot.presents(code) >= station.capacite()) {
            throw new MouvementImpossible("toutes les bornes de " + code + " sont occupées");
        }
        depot.deposer(identifiant, code);
        return new Velo(identifiant, code, EtatVelo.DISPONIBLE);
    }

    /**
     * Consolide l'indice de rotation d'une station.
     *
     * <p>L'indice vaut, en pour cent, la part des vélos rattachés à la
     * station qui sont réellement retirables.
     * La consolidation prend un temps <b>fixe et volontairement long</b>.
     * C'est l'opération que l'exploitation trouve lente.</p>
     *
     * @throws StationIntrouvable le code ne correspond à aucune station
     */
    public int indiceRotation(String code) {
        Station station = station(code);
        List<Velo> velos = depot.velosDe(station.code());
        attendre(consolidationMs);
        if (velos.isEmpty()) {
            return 0;
        }
        long retirables = velos.stream().filter(v -> v.etat() == EtatVelo.DISPONIBLE).count();
        return (int) Math.round(100.0 * retirables / velos.size());
    }

    private static void attendre(int millisecondes) {
        try {
            Thread.sleep(millisecondes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
