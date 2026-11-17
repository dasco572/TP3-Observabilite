package hai913i.tp3.parc.configuration;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hai913i.tp3.parc.depot.jpa.FabriqueEntites;
import hai913i.tp3.parc.depot.jpa.StationJpa;
import hai913i.tp3.parc.depot.jpa.VeloJpa;

/**
 * Le parc de départ.
 *
 * <p>Ces données sont <b>fixes</b>.
 * La base est en mémoire et recréée à chaque démarrage,
 * donc deux exécutions partent du même état.
 * C'est ce qui rend les scénarios comparables d'une exécution à l'autre.</p>
 *
 * <p>Le parc de départ contient 6 stations et 25 vélos, dont 21 retirables.
 * Une station est hors service, une autre n'a plus aucun vélo retirable,
 * et 4 vélos sont en maintenance.</p>
 */
@Configuration
public class DonneesInitiales {

    /** code, nom, capacité, hors service, vélos disponibles, en maintenance. */
    private record Ligne(String code, String nom, int capacite, boolean horsService,
                         int disponibles, int maintenance) {
    }

    private static final List<Ligne> PARC = List.of(
            new Ligne("ST-ARCEAUX", "Les Arceaux", 12, false, 4, 1),
            new Ligne("ST-COMEDIE", "Place de la Comédie", 20, false, 7, 0),
            new Ligne("ST-GARE", "Gare Saint-Roch", 16, false, 0, 2),
            new Ligne("ST-MOSSON", "Mosson", 10, false, 3, 0),
            new Ligne("ST-PEYROU", "Promenade du Peyrou", 8, true, 5, 0),
            new Ligne("ST-TRIOLET", "Campus Triolet", 14, false, 2, 1));

    @Bean
    ApplicationRunner chargerLeParc(StationJpa stations, VeloJpa velos) {
        return args -> {
            if (stations.count() > 0) {
                return;
            }
            int numero = 1;
            for (Ligne ligne : PARC) {
                stations.save(FabriqueEntites.station(
                        ligne.code(), ligne.nom(), ligne.capacite(), ligne.horsService()));
                for (int i = 0; i < ligne.disponibles(); i++) {
                    velos.save(FabriqueEntites.veloDisponible(
                            identifiant(numero++), ligne.code()));
                }
                for (int i = 0; i < ligne.maintenance(); i++) {
                    velos.save(FabriqueEntites.veloEnMaintenance(
                            identifiant(numero++), ligne.code()));
                }
            }
        };
    }

    private static String identifiant(int numero) {
        return String.format("V-%04d", numero);
    }
}