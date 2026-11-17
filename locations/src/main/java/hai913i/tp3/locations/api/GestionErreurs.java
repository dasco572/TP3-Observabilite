package hai913i.tp3.locations.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import hai913i.tp3.locations.parc.MouvementRefuse;
import hai913i.tp3.locations.parc.ParcIndisponible;
import hai913i.tp3.locations.parc.StationInconnue;
import hai913i.tp3.locations.service.AbonneRefuse;
import hai913i.tp3.locations.service.LocationDejaCloturee;
import hai913i.tp3.locations.service.LocationIntrouvable;

/**
 * Le seul endroit du service qui traduise une faute du métier en réponse
 * HTTP au format {@code ProblemDetail} de la RFC 9457.
 *
 * <p>Un refus du parc reste un conflit (409), tandis qu'une indisponibilité du
 * parc devient une indisponibilité de ce service (503).
 * Un appelant qui ne verrait qu'un code 500
 * ne saurait pas lequel des deux services est en cause.</p>
 */
@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler({LocationIntrouvable.class, StationInconnue.class})
    public ProblemDetail introuvable(RuntimeException faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, faute.getMessage());
    }

    @ExceptionHandler({LocationDejaCloturee.class, MouvementRefuse.class})
    public ProblemDetail conflit(RuntimeException faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, faute.getMessage());
    }

    @ExceptionHandler(AbonneRefuse.class)
    public ProblemDetail refuse(AbonneRefuse faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, faute.getMessage());
    }

    @ExceptionHandler(ParcIndisponible.class)
    public ProblemDetail indisponible(ParcIndisponible faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                                                faute.getMessage());
    }
}
