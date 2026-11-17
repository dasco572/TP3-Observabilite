package hai913i.tp3.locations.parc;

/**
 * Le résultat d'un retrait ou d'un dépôt, tel que le service du parc le
 * décrit.
 *
 * @param codeStation      station concernée
 * @param identifiantVelo  vélo déplacé
 * @param velosDisponibles vélos retirables après le mouvement
 */
public record MouvementDistant(String codeStation, String identifiantVelo,
                               int velosDisponibles) {
}
