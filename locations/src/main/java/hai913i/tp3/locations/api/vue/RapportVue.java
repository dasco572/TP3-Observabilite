package hai913i.tp3.locations.api.vue;

import hai913i.tp3.locations.service.Rapport;

/**
 * Le rapport d'exploitation d'une location, tel que l'interface web le
 * reçoit.
 *
 * @param reference       location concernée
 * @param codeDepart      station de retrait
 * @param indiceRotation  indice de rotation de cette station, en pour cent
 * @param locationsDepuis locations déjà ouvertes à cette station
 */
public record RapportVue(String reference, String codeDepart, int indiceRotation,
                         int locationsDepuis) {

    public static RapportVue de(Rapport rapport) {
        return new RapportVue(rapport.reference(), rapport.codeDepart(),
                              rapport.indiceRotation(), rapport.locationsDepuis());
    }
}