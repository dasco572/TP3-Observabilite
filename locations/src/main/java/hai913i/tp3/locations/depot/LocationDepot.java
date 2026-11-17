package hai913i.tp3.locations.depot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import hai913i.tp3.locations.domaine.Location;

/**
 * Ce que le métier attend du stockage des locations.
 *
 * <p>Le service ne connaît que cette interface
 * et ignore qu'il existe une base de données.</p>
 */
public interface LocationDepot {

    /** Toutes les locations, de la plus récente à la plus ancienne. */
    List<Location> toutes();

    /** Une location ciblée par sa référence. */
    Optional<Location> parReference(String reference);

    /** Nombre de locations ouvertes à une station donnée. */
    int comptePourDepart(String codeDepart);

    /**
     * Ouvre une location et lui attribue une référence.
     *
     * @return la location enregistrée, référence comprise
     */
    Location ouvrir(String identifiantVelo, String codeDepart, Instant debut,
                    String domaineAbonne);

    /**
     * Clôture une location.
     *
     * @return la location mise à jour
     */
    Location cloturer(String reference, String codeArrivee, Instant fin,
                      int dureeMinutes, BigDecimal montant);
}
