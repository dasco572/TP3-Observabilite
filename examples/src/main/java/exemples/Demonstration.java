package exemples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

/**
 * Les quatre mécanismes de l'instrumentation manuelle.
 *
 * <p>À lancer de deux façons, et à comparer :</p>
 *
 * <pre>
 *   java -jar target/exemples.jar
 *   java -javaagent:opentelemetry-javaagent.jar -jar target/exemples.jar
 * </pre>
 *
 * <p>Le JAR est le même. Pas une ligne de code ne change.
 * C'est tout l'argument de l'instrumentation zéro-code.</p>
 */
public final class Demonstration {

    private static final Logger log = LoggerFactory.getLogger(Demonstration.class);

    /**
     * Le tracer d'instrumentation manuelle.
     *
     * <p>Son nom et sa version identifient <b>qui</b> a produit le span.
     * Dans Jaeger, c'est ce qui distingue un span crit par vous 
     * d'un span produit par une bibliothèque.</p>
     *
     * <p>Sans agent attaché, {@code GlobalOpenTelemetry} rend une
     * implémentation no-op : tous les appels ci-dessous ne font rien,
     * et le programme s'exécute à l'identique.</p>
     */
    private static final Tracer tracer =
            GlobalOpenTelemetry.getTracer("hai913i.exemples.conversion", "1.0.0");

    private Demonstration() {
    }

    public static void main(String[] args) {
        journaliserCorrectement();
        convertirAvecUnSpanExplicite(42, "km", "mi");
        convertirAvecUneAnnotation(1, "nmi", "km");
        echouerProprement();
        log.info("fin de la demonstration");
    }

    // ------------------------------------------------------------------
    //  1. Journaliser
    // ------------------------------------------------------------------

    /** Les quatre règles d'un appel de journalisation correct. */
    private static void journaliserCorrectement() {
        double valeur = 5;

        //  (a) APPEL PARAMÉTRÉ (jamais de concaténation).
        //  Le message reste une constante :
        //  on peut le chercher, le compter, le filtrer.
        //  Et quand le niveau est désactivé, rien n'est construit.
        log.debug("conversion demandee valeur={} depuis={} vers={}", valeur, "mi", "km");

        //  (b) JOURNAL STRUCTURÉ : le message est un nom d'événement
        //  STABLE, et les valeurs sont des champs nommés.
        //  C'est ce qui rend la ligne interrogeable autrement
        //  qu'avec une expression régulière.
        log.atInfo()
           .setMessage("conversion.effectuee")
           .addKeyValue("conversion.valeur", valeur)
           .addKeyValue("conversion.unite_source", "mi")
           .addKeyValue("conversion.unite_cible", "km")
           .addKeyValue("conversion.resultat", Convertisseur.convertir(valeur, "mi", "km"))
           .log();

        //  (c) WARN = inattendu, mais le traitement CONTINUE.
        //  Si le programme s'arrête, c'est ERROR.
        //  Si tout va bien, c'est INFO ou DEBUG.
        //  Un WARN qui se déclenche à chaque passage ne dit plus
        //  rien à personne.
        if (valeur > 1000) {
            log.warn("distance inhabituelle valeur={}", valeur);
        }

        //  (d) Ce qu'on NE journalise PAS : aucune donnée d'identification,
        //  aucun secret, et aucune valeur à forte cardinalité dans le
        //  message lui-même.
    }

    // ------------------------------------------------------------------
    //  2. Un span explicite, avec attributs et événement
    // ------------------------------------------------------------------

    /**
     * La forme complète : on écrit le span parce qu'on veut y porter des
     * attributs du métier.
     */
    private static void convertirAvecUnSpanExplicite(double valeur, String depuis,
                                                     String vers) {
        Span span = tracer.spanBuilder("conversion.distance").startSpan();

        //  makeCurrent() rend ce span ACTIF : tout ce qui est instrumenté
        //  en dessous devient son enfant, et le trace_id arrive dans le MDC
        //  du journal.
        //  Sans makeCurrent(), le span existe mais rien ne s'y rattache.
        try (var ignore = span.makeCurrent()) {
            span.setAttribute("conversion.unite_source", depuis);
            span.setAttribute("conversion.unite_cible", vers);
            span.setAttribute("conversion.valeur", valeur);

            double resultat = Convertisseur.convertir(valeur, depuis, vers);

            //  Un ÉVÉNEMENT : un instant daté à l'intérieur du span.
            //  À réserver à ce qui a une position dans le temps.
            //  Une propriété du span entier est un attribut, pas un événement.
            span.addEvent("conversion.arrondie",
                          Attributes.of(AttributeKey.doubleKey("resultat"), resultat));

            //  Journalisé DANS le span.
            //  La ligne portera son trace_id et son span_id, sans que personne ne les écrive.
            log.atInfo()
               .setMessage("conversion.distance")
               .addKeyValue("conversion.resultat", resultat)
               .log();
        } finally {
            //  Toujours dans un finally.
            //  Un span qui n'est jamais fermé n'apparaît nulle part.
            span.end();
        }
    }

    // ------------------------------------------------------------------
    //  3. Un span simple par annotation
    // ------------------------------------------------------------------

    /**
     * La forme la plus légère d'un span.
     * Quand on veut seulement faire apparaître une étape,
     * l'annotation suffit.
     * C'est l'<b>agent</b> qui crée le span, et non ce code.
     *
     * <p>{@code @SpanAttribute} fait passer la valeur d'un paramètre en
     * attribut du span.
     * Sans nom explicite, il faudrait compiler avec {@code -parameters} ;
     * on nomme donc l'attribut.</p>
     *
     * <p>Sans agent, l'annotation ne fait rien du tout.</p>
     */
    @WithSpan("conversion.annotee")
    private static double convertirAvecUneAnnotation(
            double valeur,
            @SpanAttribute("conversion.unite_source") String depuis,
            @SpanAttribute("conversion.unite_cible") String vers) {
        return Convertisseur.convertir(valeur, depuis, vers);
    }

    // ------------------------------------------------------------------
    //  4. Un échec : statut du span, et exception en DERNIER argument
    // ------------------------------------------------------------------

    private static void echouerProprement() {
        Span span = tracer.spanBuilder("conversion.refusee").startSpan();
        try (var ignore = span.makeCurrent()) {
            //  Le `catch` est À L'INTÉRIEUR de la portée.
            //  En Java, le bloc `catch` d'un try-with-resources
            //  s'exécute APRÈS la fermeture de la ressource.
            //  Un `catch` écrit au niveau du try-with-resources
            //  ne verrait donc plus le span comme courant,
            //  et la ligne de journal d'erreur sortirait SANS trace_id.
            try {
                Convertisseur.convertir(1, "km", "parsec");
            } catch (Convertisseur.UniteInconnue faute) {
                //  Deux gestes distincts.

                //  (a) Marquer le SPAN en erreur, et y enregistrer
                //  l'exception.
                //  Un span qui réussit et un span qui échoue
                //  ne se lisent pas de la même façon dans Jaeger.
                span.setStatus(StatusCode.ERROR, faute.getMessage());
                span.recordException(faute);

                //  (b) Journaliser, avec l'exception en DERNIER argument et
                //  SANS marqueur dans le message.
                //  C'est à cette condition que la pile d'appels est imprimée.
                log.error("conversion.refusee unite_cible={}", "parsec", faute);
            }
        } finally {
            //  N'oubliez pas de fermer le span.
            span.end();
        }
    }
}