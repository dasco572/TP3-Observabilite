package hai913i.tp3.locations.parc;

/**
 * Le parc a répondu, et il a refusé.
 * Plus aucun vélo à retirer, plus aucune borne libre, ou vélo inconnu.
 */
public class MouvementRefuse extends RuntimeException {

    public MouvementRefuse(String message) {
        super(message);
    }
}
