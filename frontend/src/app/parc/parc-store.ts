/**
 * L'état de l'application et ses quatre gestes.
 *
 * <p>C'est la couche application du navigateur.
 * Elle sait ce que l'utilisateur veut faire,
 * enchaîne les appels au service dorsal,
 * et traduit le résultat en état affichable.
 * Elle ne connaît pas le DOM.</p>
 *
 * <p>Les composants ne font qu'afficher ce qu'elle expose et appeler ses
 * méthodes ; aucun composant n'appelle {@link ParcApi} directement.</p>
 */
import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';

import { Echec, echecDepuis } from './parc-errors';
import { ParcApi } from './parc-api';
import { Location, Rapport, Station } from './parc-models';

export type Resultat =
  | { readonly etat: 'vide' }
  | { readonly etat: 'succes'; readonly message: string }
  | { readonly etat: 'echec'; readonly echec: Echec };

@Injectable({ providedIn: 'root' })
export class ParcStore {
  private readonly api = inject(ParcApi);

  private readonly _stations = signal<readonly Station[]>([]);
  private readonly _locations = signal<readonly Location[]>([]);
  private readonly _rapport = signal<Rapport | null>(null);
  private readonly _resultat = signal<Resultat>({ etat: 'vide' });
  private readonly _enCours = signal(false);

  readonly stations = this._stations.asReadonly();
  readonly locations = this._locations.asReadonly();
  readonly rapport = this._rapport.asReadonly();
  readonly resultat = this._resultat.asReadonly();
  readonly enCours = this._enCours.asReadonly();

  readonly locationsEnCours = computed(() =>
    this._locations().filter((l) => l.etat === 'EN_COURS'),
  );

  // Les quatre gestes

  /** Charge les stations, puis les locations (l'état initial de la page). */
  rafraichir(): Promise<void> {
    return this.geste(async () => {
      const stations = await this.appeler(this.api.stations());
      const locations = await this.appeler(this.api.locations());
      this._stations.set(stations);
      this._locations.set(locations);
      const louables = stations.filter((s) => s.louable).length;
      return `${stations.length} stations, dont ${louables} louables`;
    });
  }

  louer(codeStation: string): Promise<void> {
    return this.geste(async () => {
      const location = await this.appeler(this.api.ouvrir(codeStation));
      await this.recharger();
      return `location ${location.reference} ouverte : vélo ${location.identifiantVelo}`;
    });
  }

  rendre(reference: string, codeStation: string): Promise<void> {
    return this.geste(async () => {
      const location = await this.appeler(this.api.restituer(reference, codeStation));
      await this.recharger();
      return (
        `location ${location.reference} clôturée : ${location.dureeMinutes} min, ` +
        `${location.montant} €`
      );
    });
  }

  /** Le geste long. */
  voirRapport(reference: string): Promise<void> {
    return this.geste(async () => {
      const rapport = await this.appeler(this.api.rapport(reference));
      this._rapport.set(rapport);
      return `rapport de ${rapport.reference} établi`;
    });
  }

  // l'enveloppe

  /**
   * Un geste de l'utilisateur générant un état d'attente d'un travail, un résultat,
   * et éventuellement un échec traduit.
   */
  private async geste(travail: () => Promise<string>): Promise<void> {
    this._enCours.set(true);
    this._resultat.set({ etat: 'vide' });
    try {
      this._resultat.set({ etat: 'succes', message: await travail() });
    } catch (cause) {
      this._resultat.set({ etat: 'echec', echec: echecDepuis(cause) });
    } finally {
      this._enCours.set(false);
    }
  }

  /** Recharge stations et locations après un geste qui les modifie. */
  private async recharger(): Promise<void> {
    this._stations.set(await this.appeler(this.api.stations()));
    this._locations.set(await this.appeler(this.api.locations()));
  }

  private appeler<T>(source: Observable<T>): Promise<T> {
    return firstValueFrom(source);
  }
}