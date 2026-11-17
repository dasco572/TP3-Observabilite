package hai913i.tp3.locations.parc;

import java.util.List;

/**
 * Ce que le service des locations attend du service du parc.
 *
 * <p>C'est un <b>port</b>.
 * Le métier dépend de cette interface, pas du client HTTP qui la réalise.
 * Les tests peuvent donc s'en passer,
 * et remplacer le parc par un double de quelques lignes.</p>
 */
public interface ParcDistant {

    /**
     * Toutes les stations du parc.
     *
     * @throws ParcIndisponible le parc n'a pas répondu
     */
    List<StationDistante> stations();

    /**
     * Retire un vélo d'une station.
     *
     * @throws StationInconnue  le parc ne connaît pas ce code
     * @throws MouvementRefuse  plus aucun vélo n'est retirable
     * @throws ParcIndisponible la station est hors service, ou le parc n'a
     *                          pas répondu
     */
    MouvementDistant retirer(String codeStation);

    /**
     * Dépose un vélo à une station.
     *
     * @throws StationInconnue  le parc ne connaît pas ce code
     * @throws MouvementRefuse  aucune borne n'est libre, ou le vélo est inconnu
     * @throws ParcIndisponible la station est hors service, ou le parc n'a
     *                          pas répondu
     */
    MouvementDistant deposer(String codeStation, String identifiantVelo);

    /**
     * Indice de rotation consolidé d'une station.
     *
     * <p>C'est l'appel long de l'application.</p>
     *
     * @throws StationInconnue  le parc ne connaît pas ce code
     * @throws ParcIndisponible le parc n'a pas répondu
     */
    int indiceRotation(String codeStation);
}