package hai913i.tp3.locations.api.vue;

import hai913i.tp3.locations.parc.StationDistante;

/**
 * Une station, telle que l'interface web la reçoit.
 *
 * <p>Elle ajoute à la description du parc l'information qui intéresse
 * un abonné : peut-on louer ici, maintenant ?</p>
 *
 * @param code             code de la station
 * @param nom              libellé affiché
 * @param capacite         nombre de bornes
 * @param velosDisponibles vélos retirables
 * @param horsService      vrai quand la station ne peut pas servir
 * @param louable          vrai quand un retrait peut aboutir
 */
public record StationVue(String code, String nom, int capacite,
                         int velosDisponibles, boolean horsService, boolean louable) {

    public static StationVue de(StationDistante station) {
        return new StationVue(station.code(), station.nom(), station.capacite(),
                              station.velosDisponibles(), station.horsService(),
                              !station.horsService() && station.velosDisponibles() > 0);
    }
}