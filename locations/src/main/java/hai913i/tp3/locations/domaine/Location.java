package hai913i.tp3.locations.domaine;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Une location, du retrait à la restitution.
 *
 * <p>Cet enregistrement ne contient <b>ni l'adresse électronique complète de l'abonné,
 * ni son mot de passe</b>.
 * Seul le domaine de l'adresse est conservé, parce qu'il suffit à la seule question
 * que l'exploitation se pose (d'où viennent les abonnés ?) et qu'il ne singularise personne.</p>
 *
 * @param reference       identifiant de la location, par exemple {@code L-0001}
 * @param identifiantVelo vélo retiré
 * @param codeDepart      station de retrait
 * @param codeArrivee     station de restitution, {@code null} tant que la
 *                        location est en cours
 * @param debut           instant du retrait
 * @param fin             instant de la restitution, {@code null} tant que la
 *                        location est en cours
 * @param dureeMinutes    durée facturée, {@code 0} tant que la location est
 *                        en cours
 * @param montant         montant facturé, {@code null} tant que la location
 *                        est en cours
 * @param domaineAbonne   domaine de l'adresse de l'abonné
 * @param etat            état courant de la location
 */
public record Location(String reference, String identifiantVelo,
                       String codeDepart, String codeArrivee,
                       Instant debut, Instant fin,
                       int dureeMinutes, BigDecimal montant,
                       String domaineAbonne, EtatLocation etat) {

    /** Vraie quand le vélo n'a pas encore été rendu. */
    public boolean enCours() {
        return etat == EtatLocation.EN_COURS;
    }
}
