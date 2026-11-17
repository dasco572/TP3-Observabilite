package hai913i.tp3.parc.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hai913i.tp3.parc.api.vue.DemandeDepot;
import hai913i.tp3.parc.api.vue.IndiceVue;
import hai913i.tp3.parc.api.vue.MouvementVue;
import hai913i.tp3.parc.api.vue.StationVue;
import hai913i.tp3.parc.domaine.Station;
import hai913i.tp3.parc.domaine.Velo;
import hai913i.tp3.parc.service.ServiceParc;
import jakarta.validation.Valid;

/**
 * Le bord HTTP du service du parc.
 *
 * <p>Il convertit une requête en appel de service et un résultat du métier
 * en représentation.
 * Il ne calcule rien, ne décide rien et ne traduit aucune erreur (voir {@link GestionErreurs}).</p>
 *
 * <p>Ce service n'est pas appelé par le navigateur.
 * Seul le service des locations lui parle.
 * Aucune configuration CORS n'est donc nécessaire ici.</p>
 */
@RestController
@RequestMapping("/stations")
public class ParcController {

    private final ServiceParc service;

    ParcController(ServiceParc service) {
        this.service = service;
    }

    @GetMapping
    public List<StationVue> lister() {
        return service.stations().stream().map(this::vue).toList();
    }

    @GetMapping("/{code}")
    public StationVue lire(@PathVariable String code) {
        return vue(service.station(code));
    }

    @PostMapping("/{code}/retrait")
    public MouvementVue retrait(@PathVariable String code) {
        Velo velo = service.retirer(code);
        return new MouvementVue(code, velo.identifiant(), service.disponibles(code));
    }

    @PostMapping("/{code}/depot")
    public MouvementVue depot(@PathVariable String code,
                              @Valid @RequestBody DemandeDepot demande) {
        Velo velo = service.deposer(code, demande.identifiantVelo());
        return new MouvementVue(code, velo.identifiant(), service.disponibles(code));
    }

    @GetMapping("/{code}/indice-rotation")
    public IndiceVue indiceRotation(@PathVariable String code) {
        return new IndiceVue(code, service.indiceRotation(code));
    }

    private StationVue vue(Station station) {
        return StationVue.de(station, service.disponibles(station.code()));
    }
}
