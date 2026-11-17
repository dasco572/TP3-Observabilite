package hai913i.tp3.locations.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import hai913i.tp3.locations.depot.LocationDepot;
import hai913i.tp3.locations.domaine.Location;
import hai913i.tp3.locations.domaine.Tarif;
import hai913i.tp3.locations.parc.MouvementDistant;
import hai913i.tp3.locations.parc.ParcDistant;
import hai913i.tp3.locations.parc.StationDistante;

/**
 * Les règles de la location.
 *
 * <p>C'est la couche qui porte les décisions du métier :
 * qui a le droit de louer, quel vélo sort, combien coûte un trajet.
 * Elle s'appuie sur trois collaborateurs :
 * le registre des locations,
 * le parc (derrière un port, {@link ParcDistant})
 * et une horloge.</p>
 *
 * <p>Elle ignore qu'il existe un protocole HTTP.
 * Elle rend des objets du métier ou lève une faute du métier.</p>
 */
@Service
public class ServiceLocations {

    private final LocationDepot depot;
    private final ParcDistant parc;
    private final Abonnes abonnes;
    private final Clock horloge;

    ServiceLocations(LocationDepot depot, ParcDistant parc, Abonnes abonnes, Clock horloge) {
        this.depot = depot;
        this.parc = parc;
        this.abonnes = abonnes;
        this.horloge = horloge;
    }

    /** Les stations, telles que le parc les décrit. */
    public List<StationDistante> stations() {
        return parc.stations();
    }

    public List<Location> locations() {
        return depot.toutes();
    }

    public Location location(String reference) {
        return depot.parReference(reference)
                .orElseThrow(() -> new LocationIntrouvable(reference));
    }

    /**
     * Ouvre une location.
     * Vérifie l'abonné, fait retirer un vélo par le parc,
     * et enregistre le départ.
     *
     * <p>Le mot de passe sert à {@link Abonnes#verifier} et s'arrête là.
     * De l'adresse, seul le domaine est conservé.</p>
     *
     * @throws AbonneRefuse l'abonné n'a pas été reconnu
     */
    public Location ouvrir(String codeStation, String courriel, String motDePasse) {
        String domaine = abonnes.verifier(courriel, motDePasse);
        MouvementDistant mouvement = parc.retirer(codeStation);
        return depot.ouvrir(mouvement.identifiantVelo(), codeStation,
                            horloge.instant(), domaine);
    }

    /**
     * Clôture une location.
     * Fait déposer le vélo par le parc,
     * puis calcule la durée et le montant.
     *
     * @throws LocationIntrouvable    la référence est inconnue
     * @throws LocationDejaCloturee   le vélo a déjà été rendu
     */
    public Location restituer(String reference, String codeStation) {
        Location location = location(reference);
        if (!location.enCours()) {
            throw new LocationDejaCloturee(reference);
        }
        parc.deposer(codeStation, location.identifiantVelo());
        Instant fin = horloge.instant();
        int duree = Tarif.dureeMinutes(location.debut(), fin);
        BigDecimal montant = Tarif.montant(duree);
        return depot.cloturer(reference, codeStation, fin, duree, montant);
    }

    /**
     * Établit le rapport d'exploitation d'une location.
     *
     * <p>Pour cela, il faut une donnée que seul le parc sait calculer,
     * mais la calculer prend du temps.
     * C'est l'opération dont l'exploitation se plaint.</p>
     *
     * @throws LocationIntrouvable la référence est inconnue
     */
    public Rapport rapport(String reference) {
        Location location = location(reference);
        int indice = parc.indiceRotation(location.codeDepart());
        int locationsDepuis = depot.comptePourDepart(location.codeDepart());
        return new Rapport(reference, location.codeDepart(), indice, locationsDepuis);
    }
}