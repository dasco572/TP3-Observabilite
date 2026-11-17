package hai913i.tp3.locations.depot.jpa;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hai913i.tp3.locations.depot.LocationDepot;
import hai913i.tp3.locations.domaine.Location;

/**
 * Le stockage des locations en base de données.
 *
 * <p>C'est lui qui attribue les références, dans l'ordre d'ouverture :
 * {@code L-0001}, {@code L-0002}, etc.
 * La base étant recréée à chaque démarrage,
 * la première location d'une exécution porte toujours la même référence.</p>
 */
@Repository
public class LocationDepotJpa implements LocationDepot {

    private final LocationJpa locations;

    LocationDepotJpa(LocationJpa locations) {
        this.locations = locations;
    }

    @Override
    public List<Location> toutes() {
        return locations.findAllByOrderByReferenceDesc().stream()
                .map(LocationDepotJpa::location).toList();
    }

    @Override
    public Optional<Location> parReference(String reference) {
        return locations.findById(reference).map(LocationDepotJpa::location);
    }

    @Override
    public int comptePourDepart(String codeDepart) {
        return locations.countByCodeDepart(codeDepart);
    }

    @Override
    @Transactional
    public Location ouvrir(String identifiantVelo, String codeDepart, Instant debut,
                           String domaineAbonne) {
        String reference = String.format("L-%04d", locations.count() + 1);
        LocationEntite entite = new LocationEntite(reference, identifiantVelo, codeDepart,
                                                   debut, domaineAbonne);
        return location(locations.save(entite));
    }

    @Override
    @Transactional
    public Location cloturer(String reference, String codeArrivee, Instant fin,
                             int dureeMinutes, BigDecimal montant) {
        LocationEntite entite = locations.findById(reference).orElseThrow();
        entite.cloturer(codeArrivee, fin, dureeMinutes, montant);
        return location(locations.save(entite));
    }

    private static Location location(LocationEntite e) {
        return new Location(e.getReference(), e.getIdentifiantVelo(), e.getCodeDepart(),
                            e.getCodeArrivee(), e.getDebut(), e.getFin(),
                            e.getDureeMinutes(), e.getMontant(), e.getDomaineAbonne(),
                            e.getEtat());
    }
}
