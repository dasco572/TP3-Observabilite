package hai913i.tp3.locations.parc;

/**
 * Le service du parc n'a pas pu répondre.
 *
 * <p>Deux causes différentes aboutissent ici :
 * le parc a répondu qu'il était indisponible
 * (une station hors service, par exemple),
 * ou bien il n'a pas répondu du tout.</p>
 */
public class ParcIndisponible extends RuntimeException {

    public ParcIndisponible(String message) {
        super(message);
    }

    public ParcIndisponible(String message, Throwable cause) {
        super(message, cause);
    }
}