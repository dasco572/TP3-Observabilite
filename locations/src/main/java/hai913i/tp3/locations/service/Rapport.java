package hai913i.tp3.locations.service;

/**
 * Le rapport d'exploitation d'une location.
 *
 * <p>Il croise une donnée locale
 * (le nombre de locations ouvertes à la station de départ)
 * et une donnée consolidée par le service du parc.</p>
 *
 * @param reference        location concernée
 * @param codeDepart       station de retrait
 * @param indiceRotation   indice de rotation de cette station (pourcentage)
 * @param locationsDepuis  locations déjà ouvertes à cette station
 */
public record Rapport(String reference, String codeDepart, int indiceRotation,
                      int locationsDepuis) {
}
