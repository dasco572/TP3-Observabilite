package hai913i.tp3.locations.domaine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Le tarif d'une location.
 *
 * <p>Toute minute entamée est due, et toute location est facturée au moins
 * une minute.
 * Une location ouverte et rendue dans la même minute coûte donc
 * exactement la prise en charge plus une minute.</p>
 */
public final class Tarif {

    /** Montant dû dès le retrait du vélo, en euros. */
    public static final BigDecimal PRISE_EN_CHARGE = new BigDecimal("1.00");

    /** Montant dû par minute entamée, en euros. */
    public static final BigDecimal PRIX_MINUTE = new BigDecimal("0.05");

    private Tarif() {
    }

    /** Minutes facturées entre deux instants (au moins une, arrondie au-dessus) */
    public static int dureeMinutes(Instant debut, Instant fin) {
        long secondes = Math.max(0, Duration.between(debut, fin).getSeconds());
        return (int) Math.max(1, (secondes + 59) / 60);
    }

    /** Montant facturé pour une durée donnée (arrondi au centime) */
    public static BigDecimal montant(int dureeMinutes) {
        return PRISE_EN_CHARGE
                .add(PRIX_MINUTE.multiply(BigDecimal.valueOf(dureeMinutes)))
                .setScale(2, RoundingMode.HALF_UP);
    }
}