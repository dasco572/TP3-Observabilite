/**
 * Accès au service des locations.
 *
 * <p>Pas d'état et pas de message destiné à l'utilisateur.
 * Cette couche connaît des routes et des types ;
 * elle ne sait pas pourquoi on l'appelle.</p>
 *
 * <p>C'est l'unique endroit de l'application qui connaît l'adresse du
 * service dorsal, et le seul qui manipule {@link DemandeLocation}.</p>
 */
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { CONFIGURATION } from '../core/configuration';
import {
  DemandeLocation,
  DemandeRestitution,
  Location,
  Rapport,
  Station,
} from './parc-models';

/**
 * L'abonné et son mot de passe.
 *
 * <p>Ce sont des données <b>indicatives</b>.</p>
 */
export const ABONNE = 'camille.rey@etu.umontpellier.fr';
export const MOT_DE_PASSE = 'Zx9!Vole-Tandem';

@Injectable({ providedIn: 'root' })
export class ParcApi {
  private readonly http = inject(HttpClient);
  private readonly base = inject(CONFIGURATION).urlLocations;

  stations(): Observable<Station[]> {
    return this.http.get<Station[]>(`${this.base}/stations`);
  }

  locations(): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.base}/locations`);
  }

  ouvrir(codeStation: string): Observable<Location> {
    const demande: DemandeLocation = {
      codeStation,
      courriel: ABONNE,
      motDePasse: MOT_DE_PASSE,
    };
    return this.http.post<Location>(`${this.base}/locations`, demande);
  }

  restituer(reference: string, codeStation: string): Observable<Location> {
    const demande: DemandeRestitution = { codeStation };
    return this.http.post<Location>(
      `${this.base}/locations/${reference}/restitution`,
      demande,
    );
  }

  rapport(reference: string): Observable<Rapport> {
    return this.http.get<Rapport>(`${this.base}/locations/${reference}/rapport`);
  }
}
