package hai913i.tp3.locations.service;

/** Le vélo de cette location a déjà été rendu. */
public class LocationDejaCloturee extends RuntimeException {

    public LocationDejaCloturee(String reference) {
        super("location déjà clôturée : " + reference);
    }
}
