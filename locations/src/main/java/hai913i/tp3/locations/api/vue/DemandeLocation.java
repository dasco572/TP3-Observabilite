package hai913i.tp3.locations.api.vue;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Ce qu'il faut fournir pour ouvrir une location.
 *
 * <p>La demande porte une adresse électronique complète et un mot de passe,
 * parce que le service doit reconnaître l'abonné.
 * Les deux sont de vraies données sensibles et doivent être traitées ainsi comme indiqué.</p>
 *
 * @param codeStation station où retirer le vélo
 * @param courriel    adresse de l'abonné
 * @param motDePasse  mot de passe de l'abonné
 */
public record DemandeLocation(@NotBlank String codeStation,
                              @NotBlank @Email String courriel,
                              @NotBlank String motDePasse) {
}
