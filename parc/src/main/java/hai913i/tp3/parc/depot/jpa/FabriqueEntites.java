package hai913i.tp3.parc.depot.jpa;

import hai913i.tp3.parc.domaine.EtatVelo;

/**
 * Construit des entités de persistance.
 *
 * <p>Les constructeurs des entités sont réservés au paquetage.
 * Cette fabrique est le seul point d'entrée public, utilisé par le chargement des
 * données initiales.</p>
 */
public final class FabriqueEntites {

    private FabriqueEntites() {
    }

    public static StationEntite station(String code, String nom, int capacite,
                                        boolean horsService) {
        return new StationEntite(code, nom, capacite, horsService);
    }

    public static VeloEntite veloDisponible(String identifiant, String codeStation) {
        return new VeloEntite(identifiant, codeStation, EtatVelo.DISPONIBLE);
    }

    public static VeloEntite veloEnMaintenance(String identifiant, String codeStation) {
        return new VeloEntite(identifiant, codeStation, EtatVelo.MAINTENANCE);
    }
}
