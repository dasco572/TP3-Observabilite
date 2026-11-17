package hai913i.tp3.locations.parc;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Le client HTTP du service du parc.
 *
 * <p>C'est le seul endroit du service des locations qui sache que le parc
 * est joignable par le réseau. 
 * Il traduit les codes de statut du parc en fautes du métier,
 * de sorte que la couche service n'ait jamais à lire un code HTTP.</p>
 *
 * <p>Deux situations sont distinguées :</p>
 * <ul>
 *   <li>le parc <b>a répondu</b>, et il a dit non ;</li>
 *   <li>le parc <b>n'a pas répondu</b> (arrêté, injoignable, trop lent).</li>
 * </ul>
 */
@Component
public class ClientParcHttp implements ParcDistant {

    /** Ce qu'il faut envoyer au parc pour déposer un vélo. */
    private record DemandeDepot(String identifiantVelo) {
    }

    /** Ce que le parc répond quand on lui demande un indice de rotation. */
    private record IndiceDistant(String codeStation, int indice) {
    }

    private final RestClient client;
    private final String urlParc;

    ClientParcHttp(RestClient clientParc,
                   @Value("${locations.url-parc}") String urlParc) {
        this.client = clientParc;
        this.urlParc = urlParc;
    }

    @Override
    public List<StationDistante> stations() {
        try {
            StationDistante[] recues = client.get().uri("/stations")
                    .retrieve().body(StationDistante[].class);
            return recues == null ? List.of() : List.of(recues);
        } catch (RestClientResponseException e) {
            throw traduire(null, e);
        } catch (ResourceAccessException e) {
            throw injoignable(e);
        }
    }

    @Override
    public MouvementDistant retirer(String codeStation) {
        try {
            return exiger(client.post().uri("/stations/{code}/retrait", codeStation)
                    .retrieve().body(MouvementDistant.class));
        } catch (RestClientResponseException e) {
            throw traduire(codeStation, e);
        } catch (ResourceAccessException e) {
            throw injoignable(e);
        }
    }

    @Override
    public MouvementDistant deposer(String codeStation, String identifiantVelo) {
        try {
            return exiger(client.post().uri("/stations/{code}/depot", codeStation)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DemandeDepot(identifiantVelo))
                    .retrieve().body(MouvementDistant.class));
        } catch (RestClientResponseException e) {
            throw traduire(codeStation, e);
        } catch (ResourceAccessException e) {
            throw injoignable(e);
        }
    }

    @Override
    public int indiceRotation(String codeStation) {
        try {
            return exiger(client.get().uri("/stations/{code}/indice-rotation", codeStation)
                    .retrieve().body(IndiceDistant.class)).indice();
        } catch (RestClientResponseException e) {
            throw traduire(codeStation, e);
        } catch (ResourceAccessException e) {
            throw injoignable(e);
        }
    }

    private static <T> T exiger(T corps) {
        if (corps == null) {
            throw new ParcIndisponible("le service du parc a répondu un corps vide");
        }
        return corps;
    }

    private ParcIndisponible injoignable(ResourceAccessException cause) {
        return new ParcIndisponible("service du parc injoignable : " + urlParc, cause);
    }

    /** Traduit la réponse d'erreur du parc en faute du métier. */
    private RuntimeException traduire(String codeStation, RestClientResponseException e) {
        String detail = detailDe(e);
        if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            return new StationInconnue(codeStation == null ? "?" : codeStation);
        }
        if (e.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
            return new MouvementRefuse(detail);
        }
        if (e.getStatusCode().isSameCodeAs(HttpStatus.SERVICE_UNAVAILABLE)) {
            return new ParcIndisponible(detail);
        }
        return new ParcIndisponible(
                "le service du parc a répondu " + e.getStatusCode().value() + " : " + detail);
    }

    private static String detailDe(RestClientResponseException e) {
        ProblemDetail probleme = e.getResponseBodyAs(ProblemDetail.class);
        String detail = probleme == null ? null : probleme.getDetail();
        return detail == null ? e.getStatusText() : detail;
    }
}
