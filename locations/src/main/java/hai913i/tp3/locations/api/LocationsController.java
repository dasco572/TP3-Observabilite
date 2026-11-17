package hai913i.tp3.locations.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hai913i.tp3.locations.api.vue.DemandeLocation;
import hai913i.tp3.locations.api.vue.DemandeRestitution;
import hai913i.tp3.locations.api.vue.LocationVue;
import hai913i.tp3.locations.api.vue.RapportVue;
import hai913i.tp3.locations.api.vue.StationVue;
import hai913i.tp3.locations.service.ServiceLocations;
import jakarta.validation.Valid;

/**
 * Le bord HTTP du service des locations
 * (le seul point d'entrée de l'interface web).
 *
 * <p>Il convertit une requête en appel de service et un résultat du métier
 * en représentation.
 * Il ne calcule rien, ne décide rien et ne traduit aucune erreur (voir {@link GestionErreurs}).</p>
 */
@RestController
public class LocationsController {

    private final ServiceLocations service;

    LocationsController(ServiceLocations service) {
        this.service = service;
    }

    @GetMapping("/stations")
    public List<StationVue> stations() {
        return service.stations().stream().map(StationVue::de).toList();
    }

    @GetMapping("/locations")
    public List<LocationVue> locations() {
        return service.locations().stream().map(LocationVue::de).toList();
    }

    @GetMapping("/locations/{reference}")
    public LocationVue location(@PathVariable String reference) {
        return LocationVue.de(service.location(reference));
    }

    @PostMapping("/locations")
    public ResponseEntity<LocationVue> ouvrir(@Valid @RequestBody DemandeLocation demande) {
        var location = service.ouvrir(demande.codeStation(), demande.courriel(),
                                      demande.motDePasse());
        return ResponseEntity.status(HttpStatus.CREATED).body(LocationVue.de(location));
    }

    @PostMapping("/locations/{reference}/restitution")
    public LocationVue restituer(@PathVariable String reference,
                                 @Valid @RequestBody DemandeRestitution demande) {
        return LocationVue.de(service.restituer(reference, demande.codeStation()));
    }

    @GetMapping("/locations/{reference}/rapport")
    public RapportVue rapport(@PathVariable String reference) {
        return RapportVue.de(service.rapport(reference));
    }
}
