package hai913i.tp3.parc.api.vue;

/**
 * L'indice de rotation consolidé d'une station.
 *
 * @param codeStation station concernée
 * @param indice      part des vélos retirables, en pour cent
 */
public record IndiceVue(String codeStation, int indice) {
}
