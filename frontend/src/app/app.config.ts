/**
 * Les fournisseurs de l'application.
 *
 * <ol>
 *   <li><b>la configuration</b> est un jeton injectable, pas une constante
 *       recopiée dans le code des fonctionnalités ;</li>
 *   <li><b>HttpClient</b> est fourni avec <code>withFetch()</code> : le
 *       transport est donc <code>fetch</code>, et non
 *       <code>XMLHttpRequest</code>.</li>
 * </ol>
 */
import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { CONFIGURATION, configurationParDefaut } from './core/configuration';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: CONFIGURATION, useFactory: () => configurationParDefaut() },
    provideHttpClient(withFetch()),
  ],
};
