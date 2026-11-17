/**
 * Le tableau des locations.
 *
 * <p>Composant d'affichage, comme {@link StationList},
 * qui reçoit des locations et émet ce que l'utilisateur demande.</p>
 */
import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';

import { Location, Station } from './parc-models';

/** Ce qu'une restitution demande : quelle location, et à quelle station. */
export interface Restitution {
  reference: string;
  codeStation: string;
}

@Component({
  selector: 'app-location-list',
  templateUrl: './location-list.html',
  styleUrl: './location-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LocationList {
  readonly locations = input.required<readonly Location[]>();
  readonly stations = input.required<readonly Station[]>();
  readonly figee = input(false);

  readonly rendre = output<Restitution>();
  readonly rapport = output<string>();

  /** Station de retour proposée par défaut (la première qui a une borne libre). */
  stationDeRetour(location: Location): string {
    const candidate = this.stations().find(
      (s) => !s.horsService && s.code !== location.codeDepart,
    );
    return candidate ? candidate.code : location.codeDepart;
  }
}
