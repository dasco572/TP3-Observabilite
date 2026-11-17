package hai913i.tp3.locations.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import hai913i.tp3.locations.domaine.Tarif;

/** Les deux règles du tarif, prises une par une. */
class TarifTest {

    private static final Instant DEPART = Instant.parse("2026-11-04T09:00:00Z");

    @Test
    void facture_au_moins_une_minute() {
        assertThat(Tarif.dureeMinutes(DEPART, DEPART)).isEqualTo(1);
        assertThat(Tarif.dureeMinutes(DEPART, DEPART.plusSeconds(1))).isEqualTo(1);
        assertThat(Tarif.dureeMinutes(DEPART, DEPART.plusSeconds(60))).isEqualTo(1);
    }

    @Test
    void facture_toute_minute_entamee() {
        assertThat(Tarif.dureeMinutes(DEPART, DEPART.plusSeconds(61))).isEqualTo(2);
        assertThat(Tarif.dureeMinutes(DEPART, DEPART.plusSeconds(600))).isEqualTo(10);
        assertThat(Tarif.dureeMinutes(DEPART, DEPART.plusSeconds(601))).isEqualTo(11);
    }

    @Test
    void ne_facture_jamais_une_duree_negative() {
        assertThat(Tarif.dureeMinutes(DEPART, DEPART.minusSeconds(600))).isEqualTo(1);
    }

    @Test
    void ajoute_la_prise_en_charge_au_prix_des_minutes() {
        assertThat(Tarif.montant(1)).isEqualByComparingTo("1.05");
        assertThat(Tarif.montant(10)).isEqualByComparingTo("1.50");
        assertThat(Tarif.montant(60)).isEqualByComparingTo("4.00");
    }
}
