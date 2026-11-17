/**
 * La coquille de l'application contenant un titre et la page.
 *
 * <p>Pas de routeur car il n'y a qu'une page.</p>
 */
import { ChangeDetectionStrategy, Component } from '@angular/core';

import { ParcPage } from './parc/parc-page';

@Component({
  selector: 'app-root',
  imports: [ParcPage],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
