package hai913i.tp3.parc.domaine;

/**
 * Un vélo du parc.
 *
 * @param identifiant identifiant stable, par exemple {@code V-0147}
 * @param codeStation station où il est attaché, {@code null} s'il est en course
 * @param etat        état courant
 */
public record Velo(String identifiant, String codeStation, EtatVelo etat) {
}
