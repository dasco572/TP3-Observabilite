package hai913i.tp3.parc.api.vue;

import hai913i.tp3.parc.domaine.Station;

/**
 * Une station, telle qu'elle sort du service.
 *
 * @param code              code de la station
 * @param nom               libellé affiché
 * @param capacite          nombre de bornes
 * @param velosDisponibles  vélos retirables à cet instant
 * @param horsService       vrai quand la station ne peut pas servir
 */
public record StationVue(String code, String nom, int capacite,
                         int velosDisponibles, boolean horsService) {

    public static StationVue de(Station station, int velosDisponibles) {
        return new StationVue(station.code(), station.nom(), station.capacite(),
                              velosDisponibles, station.horsService());
    }
}
