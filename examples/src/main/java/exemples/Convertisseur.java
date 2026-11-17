package exemples;

import java.util.Map;

/**
 * Le micro-domaine de ces exemples : convertir une distance d'une unité
 * vers une autre.
 *
 * <p>Cette classe sert uniquement pour le calcul : elle ne journalise
 * rien et ne crée aucun span.
 * Ce qui s'observe est décidé au-dessus, dans {@link Demonstration}.</p>
 */
public final class Convertisseur {

    /** Facteur de conversion de chaque unité vers le mètre. */
    private static final Map<String, Double> VERS_METRE = Map.of(
            "m", 1.0,
            "km", 1000.0,
            "mi", 1609.344,
            "nmi", 1852.0,
            "ft", 0.3048);

    private Convertisseur() {
    }

    /** Les unités connues, pour un message d'erreur utile. */
    public static String unitesConnues() {
        return String.join(", ", VERS_METRE.keySet().stream().sorted().toList());
    }

    /**
     * Convertit une distance.
     *
     * @throws UniteInconnue l'une des deux unités n'est pas reconnue
     */
    public static double convertir(double valeur, String depuis, String vers) {
        Double facteurDepuis = VERS_METRE.get(depuis);
        Double facteurVers = VERS_METRE.get(vers);
        if (facteurDepuis == null) {
            throw new UniteInconnue(depuis);
        }
        if (facteurVers == null) {
            throw new UniteInconnue(vers);
        }
        return valeur * facteurDepuis / facteurVers;
    }

    /** L'unité demandée n'est pas connue. */
    public static class UniteInconnue extends RuntimeException {
        public UniteInconnue(String unite) {
            super("unité inconnue : " + unite + " (connues : " + unitesConnues() + ")");
        }
    }
}
