package hai913i.tp3.locations.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Les abonnés du service, et la vérification de leur mot de passe.
 *
 * <p>Les mots de passe ne sont pas stockés, mais seule leur empreinte l'est, et
 * la comparaison se fait à temps constant.
 * Un composant d'authentification d'une application réelle
 * utiliserait une fonction de dérivation lente plutôt qu'un simple condensat.
 * Ce n'est pas le sujet de ce projet, donc on simplifie ici.</p>
 *
 * <p><b>le mot de passe fourni ne quitte jamais cette classe</b>,
 * et il n'apparaît dans aucun message d'erreur.</p>
 */
@Component
public class Abonnes {

    /** adresse de l'abonné, empreinte hexadécimale SHA-256 de son mot de passe. */
    private static final Map<String, String> EMPREINTES = Map.of(
            "camille.rey@etu.umontpellier.fr",
            "cf2225169d39e4d734df27959f58db4803d00228a0a8825d3e60994a0dfd5eec",
            "noam.ferrand@etu.umontpellier.fr",
            "b32c6cc94f2620a59a0b61fa4e314efa841a2f79a20690c5861c309d89b2153f");

    /**
     * Vérifie l'abonné.
     *
     * @return le domaine de l'adresse (seule information conservée ensuite)
     * @throws AbonneRefuse l'adresse est inconnue ou le mot de passe est faux
     */
    public String verifier(String courriel, String motDePasse) {
        String attendue = courriel == null ? null : EMPREINTES.get(courriel);
        String obtenue = empreinte(motDePasse);
        if (attendue == null || !MessageDigest.isEqual(
                attendue.getBytes(StandardCharsets.UTF_8),
                obtenue.getBytes(StandardCharsets.UTF_8))) {
            throw new AbonneRefuse();
        }
        return domaineDe(courriel);
    }

    /** Ne garde que le domaine : « a.b@exemple.fr » devient « exemple.fr ». */
    public static String domaineDe(String courriel) {
        if (courriel == null) {
            return "inconnu";
        }
        int arobase = courriel.lastIndexOf('@');
        return arobase < 0 ? "invalide" : courriel.substring(arobase + 1);
    }

    private static String empreinte(String motDePasse) {
        if (motDePasse == null) {
            return "";
        }
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    sha.digest(motDePasse.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
