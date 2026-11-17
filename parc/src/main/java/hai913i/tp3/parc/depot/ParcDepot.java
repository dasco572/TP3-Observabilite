package hai913i.tp3.parc.depot;

import java.util.List;
import java.util.Optional;

import hai913i.tp3.parc.domaine.Station;
import hai913i.tp3.parc.domaine.Velo;

/**
 * Ce que le métier attend du stockage.
 *
 * <p>Le service ne connaît que cette interface
 * et ignore qu'il existe une base de données ou Hibernate.
 * L'implémentation JPA vit dans {@code depot.jpa}.</p>
 */
public interface ParcDepot {

    List<Station> stations();

    Optional<Station> parCode(String code);

    /** Vélos attachés à une station, quel que soit leur état. */
    List<Velo> velosDe(String code);

    /** Nombre de vélos retirables d'une station. */
    int disponibles(String code);

    /** Nombre de vélos présents sur les bornes, maintenance comprise. */
    int presents(String code);

    /** Le premier vélo disponible d'une station, par ordre d'identifiant. */
    Optional<Velo> premierDisponible(String code);

    Optional<Velo> velo(String identifiant);

    /** Détache un vélo de sa station et le passe en course. */
    void retirer(String identifiant);

    /** Rattache un vélo à une station et le repasse en disponible. */
    void deposer(String identifiant, String code);

    /** Total des vélos du parc, tous états confondus. */
    int totalVelos();
}
