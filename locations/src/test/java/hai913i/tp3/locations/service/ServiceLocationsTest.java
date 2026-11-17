package hai913i.tp3.locations.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import hai913i.tp3.locations.depot.LocationDepot;
import hai913i.tp3.locations.domaine.EtatLocation;
import hai913i.tp3.locations.domaine.Location;
import hai913i.tp3.locations.parc.MouvementDistant;
import hai913i.tp3.locations.parc.ParcDistant;
import hai913i.tp3.locations.parc.ParcIndisponible;
import hai913i.tp3.locations.parc.StationDistante;

/**
 * Les règles de la location, sans Spring, sans base et sans réseau.
 *
 * <p>Si le parc n'était pas derrière un port, il faudrait démarrer deux applications
 * pour vérifier un calcul de facturation.</p>
 */
class ServiceLocationsTest {

    private static final Instant DEPART = Instant.parse("2026-11-04T09:00:00Z");
    private static final String COURRIEL = "camille.rey@etu.umontpellier.fr";
    private static final String MOT_DE_PASSE = "Zx9" + "!" + "Vole-Tandem";

    /** Registre en mémoire, qui attribue les mêmes références que le vrai. */
    private static final class DepotMemoire implements LocationDepot {
        private final List<Location> locations = new ArrayList<>();

        @Override
        public List<Location> toutes() {
            return List.copyOf(locations);
        }

        @Override
        public Optional<Location> parReference(String reference) {
            return locations.stream().filter(l -> l.reference().equals(reference)).findFirst();
        }

        @Override
        public int comptePourDepart(String codeDepart) {
            return (int) locations.stream()
                    .filter(l -> l.codeDepart().equals(codeDepart)).count();
        }

        @Override
        public Location ouvrir(String identifiantVelo, String codeDepart, Instant debut,
                               String domaineAbonne) {
            Location location = new Location(
                    String.format("L-%04d", locations.size() + 1), identifiantVelo,
                    codeDepart, null, debut, null, 0, null, domaineAbonne,
                    EtatLocation.EN_COURS);
            locations.add(location);
            return location;
        }

        @Override
        public Location cloturer(String reference, String codeArrivee, Instant fin,
                                 int dureeMinutes, BigDecimal montant) {
            Location avant = parReference(reference).orElseThrow();
            Location apres = new Location(avant.reference(), avant.identifiantVelo(),
                    avant.codeDepart(), codeArrivee, avant.debut(), fin, dureeMinutes,
                    montant, avant.domaineAbonne(), EtatLocation.CLOTUREE);
            locations.replaceAll(l -> l.reference().equals(reference) ? apres : l);
            return apres;
        }
    }

    /** Parc de substitution. */
    private static final class ParcDouble implements ParcDistant {
        private final List<String> appels = new ArrayList<>();
        private boolean indisponible;

        @Override
        public List<StationDistante> stations() {
            appels.add("stations");
            return List.of(new StationDistante("ST-COMEDIE", "Place de la Comédie",
                                               20, 7, false));
        }

        @Override
        public MouvementDistant retirer(String codeStation) {
            appels.add("retirer:" + codeStation);
            if (indisponible) {
                throw new ParcIndisponible("station hors service : " + codeStation);
            }
            return new MouvementDistant(codeStation, "V-0006", 6);
        }

        @Override
        public MouvementDistant deposer(String codeStation, String identifiantVelo) {
            appels.add("deposer:" + codeStation + ":" + identifiantVelo);
            return new MouvementDistant(codeStation, identifiantVelo, 5);
        }

        @Override
        public int indiceRotation(String codeStation) {
            appels.add("indice:" + codeStation);
            return 100;
        }
    }

    /** Horloge qui avance d'un pas fixe à chaque lecture. */
    private static final class HorlogePas extends Clock {
        private Instant maintenant = DEPART;
        private Duration pas = Duration.ZERO;

        @Override
        public Instant instant() {
            Instant lu = maintenant;
            maintenant = maintenant.plus(pas);
            return lu;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final DepotMemoire depot = new DepotMemoire();
    private final ParcDouble parc = new ParcDouble();
    private final HorlogePas horloge = new HorlogePas();
    private final ServiceLocations service =
            new ServiceLocations(depot, parc, new Abonnes(), horloge);

    @Test
    void ouvre_une_location_et_fait_retirer_un_velo() {
        Location location = service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);

        assertThat(location.reference()).isEqualTo("L-0001");
        assertThat(location.identifiantVelo()).isEqualTo("V-0006");
        assertThat(location.codeDepart()).isEqualTo("ST-COMEDIE");
        assertThat(location.enCours()).isTrue();
        assertThat(parc.appels).containsExactly("retirer:ST-COMEDIE");
    }

    @Test
    void ne_conserve_que_le_domaine_de_l_abonne() {
        Location location = service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);

        assertThat(location.domaineAbonne()).isEqualTo("etu.umontpellier.fr");
        assertThat(location.toString()).doesNotContain(MOT_DE_PASSE);
        assertThat(location.toString()).doesNotContain(COURRIEL);
    }

    @Test
    void refuse_un_abonne_inconnu_sans_appeler_le_parc() {
        assertThatThrownBy(() -> service.ouvrir("ST-COMEDIE", "inconnu@exemple.fr", "x"))
                .isInstanceOf(AbonneRefuse.class);
        assertThat(parc.appels).isEmpty();
    }

    @Test
    void refuse_un_mot_de_passe_faux() {
        assertThatThrownBy(() -> service.ouvrir("ST-COMEDIE", COURRIEL, "faux"))
                .isInstanceOf(AbonneRefuse.class);
    }

    @Test
    void facture_une_minute_minimum() {
        service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);
        Location cloturee = service.restituer("L-0001", "ST-ARCEAUX");

        assertThat(cloturee.dureeMinutes()).isEqualTo(1);
        assertThat(cloturee.montant()).isEqualByComparingTo("1.05");
        assertThat(cloturee.codeArrivee()).isEqualTo("ST-ARCEAUX");
        assertThat(cloturee.etat()).isEqualTo(EtatLocation.CLOTUREE);
    }

    @Test
    void facture_toute_minute_entamee() {
        horloge.pas = Duration.ofSeconds(601);
        service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);
        Location cloturee = service.restituer("L-0001", "ST-ARCEAUX");

        assertThat(cloturee.dureeMinutes()).isEqualTo(11);
        assertThat(cloturee.montant()).isEqualByComparingTo("1.55");
    }

    @Test
    void refuse_de_restituer_deux_fois() {
        service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);
        service.restituer("L-0001", "ST-ARCEAUX");

        assertThatThrownBy(() -> service.restituer("L-0001", "ST-ARCEAUX"))
                .isInstanceOf(LocationDejaCloturee.class);
    }

    @Test
    void refuse_une_reference_inconnue() {
        assertThatThrownBy(() -> service.restituer("L-9999", "ST-ARCEAUX"))
                .isInstanceOf(LocationIntrouvable.class);
    }

    @Test
    void laisse_remonter_l_indisponibilite_du_parc() {
        parc.indisponible = true;
        assertThatThrownBy(() -> service.ouvrir("ST-PEYROU", COURRIEL, MOT_DE_PASSE))
                .isInstanceOf(ParcIndisponible.class);
        assertThat(depot.toutes()).isEmpty();
    }

    @Test
    void croise_une_donnee_locale_et_une_donnee_du_parc_dans_le_rapport() {
        service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);
        service.ouvrir("ST-COMEDIE", COURRIEL, MOT_DE_PASSE);

        Rapport rapport = service.rapport("L-0001");

        assertThat(rapport.indiceRotation()).isEqualTo(100);
        assertThat(rapport.locationsDepuis()).isEqualTo(2);
        assertThat(parc.appels).contains("indice:ST-COMEDIE");
    }
}
