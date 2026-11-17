/**
 * Configuration de l'application désignant un seul endroit pour les constantes
 * d'infrastructure.
 *
 * <p>Aucune URL ne doit être écrite en dur dans le code d'une
 * fonctionnalité.</p>
 *
 * <p>Le mécanisme recommandé par Angular aujourd'hui :
 * un <code>InjectionToken</code> fourni par l'application.</p>
 */
import { InjectionToken } from '@angular/core';

export interface ConfigurationApplication {
  /** Origine du service des locations (le seul service que l'on appelle). */
  readonly urlLocations: string;

  /** Identité de cette application. */
  readonly nomService: string;

  readonly versionService: string;
}

export const CONFIGURATION = new InjectionToken<ConfigurationApplication>(
  'configuration de l application',
);

export function configurationParDefaut(): ConfigurationApplication {
  return {
    urlLocations: 'http://localhost:8080',
    nomService: 'velos-frontend',
    versionService: '1.0.0',
  };
}