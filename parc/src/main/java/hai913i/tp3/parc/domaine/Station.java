package hai913i.tp3.parc.domaine;

/**
 * Une station du parc.
 *
 * <p>La capacité est le nombre de bornes.
 * Elle borne le nombre de vélos que la station peut accueillir.</p>
 *
 * @param code        code court et stable, par exemple {@code ST-COMEDIE}
 * @param nom         libellé affiché
 * @param capacite    nombre de bornes
 * @param horsService vrai quand la station est hors service
 */
public record Station(String code, String nom, int capacite, boolean horsService) {
}
