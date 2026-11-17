package hai913i.tp3.locations.api.vue;

import jakarta.validation.constraints.NotBlank;

/**
 * Ce qu'il faut fournir pour rendre un vélo.
 *
 * @param codeStation station où déposer le vélo
 */
public record DemandeRestitution(@NotBlank String codeStation) {
}
