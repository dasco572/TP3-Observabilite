package hai913i.tp3.parc.api.vue;

import jakarta.validation.constraints.NotBlank;

/**
 * Ce qu'il faut fournir pour déposer un vélo.
 *
 * @param identifiantVelo identifiant du vélo à rattacher à la station
 */
public record DemandeDepot(@NotBlank String identifiantVelo) {
}
