package hai913i.tp3.locations.api.vue;

import java.math.BigDecimal;

import hai913i.tp3.locations.domaine.Location;

/**
 * Une location, telle que l'interface web la reçoit.
 * <p>Le domaine de l'abonné n'en fait pas partie.</p>
 *
 * @param reference       référence de la location
 * @param identifiantVelo vélo retiré
 * @param codeDepart      station de retrait
 * @param codeArrivee     station de restitution, {@code null} si en cours
 * @param dureeMinutes    durée facturée, {@code 0} si en cours
 * @param montant         montant facturé, {@code null} si en cours
 * @param etat            {@code EN_COURS} ou {@code CLOTUREE}
 */
public record LocationVue(String reference, String identifiantVelo,
                          String codeDepart, String codeArrivee,
                          int dureeMinutes, BigDecimal montant, String etat) {

    public static LocationVue de(Location location) {
        return new LocationVue(location.reference(), location.identifiantVelo(),
                               location.codeDepart(), location.codeArrivee(),
                               location.dureeMinutes(), location.montant(),
                               location.etat().name());
    }
}
