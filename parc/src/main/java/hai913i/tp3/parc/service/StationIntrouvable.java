package hai913i.tp3.parc.service;

/** Aucune station ne porte ce code. */
public class StationIntrouvable extends RuntimeException {

    private final String code;

    public StationIntrouvable(String code) {
        super("station introuvable : " + code);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
