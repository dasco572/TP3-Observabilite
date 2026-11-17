package hai913i.tp3.parc.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import hai913i.tp3.parc.service.MouvementImpossible;
import hai913i.tp3.parc.service.StationIndisponible;
import hai913i.tp3.parc.service.StationIntrouvable;

/**
 * Le seul endroit du service qui traduise une faute du métier en réponse
 * HTTP au format {@code ProblemDetail} de la RFC 9457.
 *
 */
@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(StationIntrouvable.class)
    public ProblemDetail introuvable(StationIntrouvable faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, faute.getMessage());
    }

    @ExceptionHandler(MouvementImpossible.class)
    public ProblemDetail conflit(MouvementImpossible faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, faute.getMessage());
    }

    @ExceptionHandler(StationIndisponible.class)
    public ProblemDetail indisponible(StationIndisponible faute) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, faute.getMessage());
    }
}