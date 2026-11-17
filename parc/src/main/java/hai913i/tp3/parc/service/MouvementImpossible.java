package hai913i.tp3.parc.service;

/**
 * Le mouvement demandé contredit l'état du parc.
 * Plus aucun vélo à retirer, ou plus aucune borne libre pour en déposer un.
 */
public class MouvementImpossible extends RuntimeException {

    public MouvementImpossible(String message) {
        super(message);
    }
}