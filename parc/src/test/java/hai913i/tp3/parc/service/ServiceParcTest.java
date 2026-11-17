package hai913i.tp3.parc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import hai913i.tp3.parc.domaine.EtatVelo;
import hai913i.tp3.parc.domaine.Station;

/**
 * Les règles du parc, sur les données de départ.
 *
 * <p>La consolidation est ramenée à zéro milliseconde,
 * vu que ces tests portent sur les règles et pas sur la durée.</p>
 *
 * <p>{@code @Transactional} fait annuler la transaction après chaque test.
 * Les méthodes qui déplacent un vélo ne se gênent donc pas entre elles, et
 * l'ordre d'exécution n'a aucune influence sur le résultat.</p>
 */
@SpringBootTest
@TestPropertySource(properties = "parc.consolidation-ms=0")
@Transactional
class ServiceParcTest {

    @Autowired
    private ServiceParc service;

    @Test
    void charge_six_stations() {
        assertThat(service.stations()).hasSize(6);
        assertThat(service.stations().stream().map(Station::code))
                .containsExactly("ST-ARCEAUX", "ST-COMEDIE", "ST-GARE",
                                 "ST-MOSSON", "ST-PEYROU", "ST-TRIOLET");
    }

    @Test
    void compte_les_velos_retirables_station_par_station() {
        assertThat(service.disponibles("ST-ARCEAUX")).isEqualTo(4);
        assertThat(service.disponibles("ST-COMEDIE")).isEqualTo(7);
        assertThat(service.disponibles("ST-GARE")).isZero();
        assertThat(service.disponibles("ST-MOSSON")).isEqualTo(3);
        assertThat(service.disponibles("ST-PEYROU")).isEqualTo(5);
        assertThat(service.disponibles("ST-TRIOLET")).isEqualTo(2);
    }

    @Test
    void retire_le_premier_velo_disponible() {
        var velo = service.retirer("ST-COMEDIE");
        assertThat(velo.identifiant()).isEqualTo("V-0006");
        assertThat(velo.etat()).isEqualTo(EtatVelo.EN_COURSE);
        assertThat(service.disponibles("ST-COMEDIE")).isEqualTo(6);
    }

    @Test
    void refuse_de_retirer_a_une_station_sans_velo() {
        assertThatThrownBy(() -> service.retirer("ST-GARE"))
                .isInstanceOf(MouvementImpossible.class);
    }

    @Test
    void refuse_de_servir_une_station_hors_service() {
        assertThatThrownBy(() -> service.retirer("ST-PEYROU"))
                .isInstanceOf(StationIndisponible.class);
    }

    @Test
    void refuse_une_station_inconnue() {
        assertThatThrownBy(() -> service.station("ST-INCONNUE"))
                .isInstanceOf(StationIntrouvable.class);
    }

    @Test
    void depose_un_velo_en_course_a_une_autre_station() {
        var retire = service.retirer("ST-MOSSON");
        var depose = service.deposer("ST-ARCEAUX", retire.identifiant());
        assertThat(depose.codeStation()).isEqualTo("ST-ARCEAUX");
        assertThat(depose.etat()).isEqualTo(EtatVelo.DISPONIBLE);
        assertThat(service.disponibles("ST-ARCEAUX")).isEqualTo(5);
    }

    @Test
    void refuse_de_deposer_un_velo_qui_n_est_pas_en_course() {
        assertThatThrownBy(() -> service.deposer("ST-ARCEAUX", "V-0006"))
                .isInstanceOf(MouvementImpossible.class);
    }

    @Test
    void consolide_un_indice_de_rotation_par_station() {
        assertThat(service.indiceRotation("ST-ARCEAUX")).isEqualTo(80);
        assertThat(service.indiceRotation("ST-COMEDIE")).isEqualTo(100);
        assertThat(service.indiceRotation("ST-GARE")).isZero();
        assertThat(service.indiceRotation("ST-TRIOLET")).isEqualTo(67);
    }
}