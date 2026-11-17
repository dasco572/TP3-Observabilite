package hai913i.tp3.parc.service;

/**
 * La station existe mais ne peut pas servir ; elle est hors service.
 *
 * <p>C'est une indisponibilité temporaire du côté du service, et non une
 * erreur de l'appelant.</p>
 */
public class StationIndisponible extends RuntimeException {

    private final String code;

    public StationIndisponible(String code) {
        super("station hors service : " + code);
        this.code = code;
    }

    public String code() {
        return code;
    }
}