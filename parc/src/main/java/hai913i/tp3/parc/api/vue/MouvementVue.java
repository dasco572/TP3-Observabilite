package hai913i.tp3.parc.api.vue;

/**
 * Le résultat d'un retrait ou d'un dépôt.
 *
 * @param codeStation       station concernée
 * @param identifiantVelo   vélo déplacé
 * @param velosDisponibles  vélos retirables après le mouvement
 */
public record MouvementVue(String codeStation, String identifiantVelo,
                           int velosDisponibles) {
}
