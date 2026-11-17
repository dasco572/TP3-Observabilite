package hai913i.tp3.locations.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import hai913i.tp3.locations.service.ServiceLocations;

/**
 * Le contexte démarre, et le service est bien câblé.
 *
 * <p>Aucun appel au parc n'est fait ici.
 * Ce test vérifie l'assemblage, pas le comportement.
 * Les règles sont vérifiées sans Spring dans {@code ServiceLocationsTest}.</p>
 */
@SpringBootTest
class ApplicationTest {

    @Autowired
    private ServiceLocations service;

    @Test
    void le_contexte_demarre_et_le_registre_est_vide() {
        assertThat(service).isNotNull();
        assertThat(service.locations()).isEmpty();
    }
}