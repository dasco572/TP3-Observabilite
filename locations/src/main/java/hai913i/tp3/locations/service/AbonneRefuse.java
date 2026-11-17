package hai913i.tp3.locations.service;

/**
 * L'abonné n'a pas été reconnu.
 *
 * <p>Le message ne dit <b>pas</b> laquelle des deux informations est fausse,
 * et il ne reprend ni l'adresse ni le mot de passe fournis.</p>
 */
public class AbonneRefuse extends RuntimeException {

    public AbonneRefuse() {
        super("abonné ou mot de passe incorrect");
    }
}
