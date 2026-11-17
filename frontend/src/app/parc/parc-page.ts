/**
 * La page.
 * Elle câble l'état aux deux tableaux.
 *
 * <p>Elle ne contient aucune règle.
 * Elle charge l'état au premier affichage,
 * transmet les gestes au magasin,
 * et affiche le résultat du dernier geste.</p>
 */
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';

import { LocationList, Restitution } from './location-list';
import { ParcStore } from './parc-store';
import { StationList } from './station-list';

@Component({
  selector: 'app-parc-page',
  imports: [StationList, LocationList],
  templateUrl: './parc-page.html',
  styleUrl: './parc-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ParcPage implements OnInit {
  private readonly store = inject(ParcStore);

  readonly stations = this.store.stations;
  readonly locations = this.store.locations;
  readonly rapport = this.store.rapport;
  readonly resultat = this.store.resultat;
  readonly enCours = this.store.enCours;

  ngOnInit(): void {
    void this.store.rafraichir();
  }

  rafraichir(): void {
    void this.store.rafraichir();
  }

  louer(codeStation: string): void {
    void this.store.louer(codeStation);
  }

  rendre(restitution: Restitution): void {
    void this.store.rendre(restitution.reference, restitution.codeStation);
  }

  voirRapport(reference: string): void {
    void this.store.voirRapport(reference);
  }
}
