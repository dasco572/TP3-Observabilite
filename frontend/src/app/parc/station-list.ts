/**
 * Le tableau des stations.
 *
 * <p>Un composant d'affichage qui reçoit des stations
 * et émet un code quand on clique.
 * Il ne connaît ni le service dorsal ni l'état global.</p>
 */
import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';

import { Station } from './parc-models';

@Component({
  selector: 'app-station-list',
  templateUrl: './station-list.html',
  styleUrl: './station-list.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StationList {
  readonly stations = input.required<readonly Station[]>();
  readonly figee = input(false);
  readonly louer = output<string>();
}