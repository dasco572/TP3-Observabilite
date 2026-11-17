package hai913i.tp3.locations.parc;

/** Le parc ne connaît aucune station portant ce code. */
public class StationInconnue extends RuntimeException {

    public StationInconnue(String code) {
        super("station inconnue du parc : " + code);
    }
}
