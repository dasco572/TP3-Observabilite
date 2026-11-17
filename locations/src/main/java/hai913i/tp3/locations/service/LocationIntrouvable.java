package hai913i.tp3.locations.service;

/** Aucune location ne porte cette référence. */
public class LocationIntrouvable extends RuntimeException {

    public LocationIntrouvable(String reference) {
        super("location introuvable : " + reference);
    }
}
