/**
 * Les échecs.
 *
 * <p>Sans ce fichier, chaque geste de l'application finirait par avoir
 * son propre <code>if (status === 503)</code>.</p>
 *
 * <p>On distingue trois catégories d'échecs.</p>
 */
import { HttpErrorResponse } from '@angular/common/http';

export type CategorieEchec =
  /** Le service n'a pas répondu : arrêté, injoignable, CORS refusé. */
  | 'injoignable'
  /** Il a répondu, et il a dit non. C'est une réponse, pas une panne. */
  | 'refus-du-service'
  /** Ni l'un ni l'autre : le défaut est de notre côté. */
  | 'inattendu';

export interface Echec {
  readonly categorie: CategorieEchec;
  readonly message: string;
  /** Présent seulement quand le service a répondu. */
  readonly statut?: number;
}

/** Les services répondent leurs erreurs en ProblemDetail (RFC 9457). */
interface ProblemDetail {
  detail?: string;
  title?: string;
}

export function echecDepuis(cause: unknown): Echec {
  if (cause instanceof HttpErrorResponse) {
    //  Le statut 0 indique que la réponse n'est jamais arrivée jusqu'à nous,
    //  faute d'un service arrêté, réseau coupé, ou contrôle préalable CORS refusé.
    //  Le confondre avec une erreur applicative rend le diagnostic faux.
    if (cause.status === 0) {
      return {
        categorie: 'injoignable',
        message:
          'service des locations injoignable — vérifiez qu\'il écoute sur le port 8080',
      };
    }
    const corps = cause.error as ProblemDetail | string | null;
    const detail =
      typeof corps === 'object' && corps !== null
        ? (corps.detail ?? corps.title)
        : undefined;
    return {
      categorie: 'refus-du-service',
      message: detail ?? `le service a répondu ${cause.status}`,
      statut: cause.status,
    };
  }
  return {
    categorie: 'inattendu',
    message: cause instanceof Error ? cause.message : String(cause),
  };
}