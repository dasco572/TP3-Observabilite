package hai913i.tp3.locations.parc;

/**
 * Une station, telle que le service du parc la décrit.
 *
 * @param code             code de la station
 * @param nom              libellé affiché
 * @param capacite         nombre de bornes
 * @param velosDisponibles vélos retirables
 * @param horsService      vrai quand la station ne peut pas servir
 */
public record StationDistante(String code, String nom, int capacite,
                              int velosDisponibles, boolean horsService) {
}
