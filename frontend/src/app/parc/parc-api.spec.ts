/**
 * L'accès au service dorsal : routes, charges utiles, échecs.
 *
 * <p><code>provideHttpClientTesting</code> remplace le transport,
 * et <code>HttpTestingController</code> permet d'affirmer ce qui
 * est parti et ce qui est revenu.</p>
 */
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { CONFIGURATION } from '../core/configuration';
import { ParcApi } from './parc-api';
import { DemandeLocation, DemandeRestitution } from './parc-models';

const DORSAL = 'http://dorsal.test';

describe('ParcApi', () => {
  let api: ParcApi;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: CONFIGURATION,
          useValue: {
            urlLocations: DORSAL,
            nomService: 'velos-frontend',
            versionService: 'test',
          },
        },
      ],
    });
    api = TestBed.inject(ParcApi);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lit les stations sur GET /stations', async () => {
    const promesse = firstValueFrom(api.stations());
    const attendu = [
      {
        code: 'ST-COMEDIE',
        nom: 'Place de la Comédie',
        capacite: 20,
        velosDisponibles: 7,
        horsService: false,
        louable: true,
      },
    ];
    const requete = httpMock.expectOne(`${DORSAL}/stations`);

    expect(requete.request.method).toBe('GET');
    requete.flush(attendu);
    expect(await promesse).toEqual(attendu);
  });

  it('lit les locations sur GET /locations', async () => {
    const promesse = firstValueFrom(api.locations());
    const requete = httpMock.expectOne(`${DORSAL}/locations`);

    expect(requete.request.method).toBe('GET');
    requete.flush([]);
    expect(await promesse).toEqual([]);
  });

  it('poste une demande portant volontairement le secret', async () => {
    const promesse = firstValueFrom(api.ouvrir('ST-COMEDIE'));
    const requete = httpMock.expectOne(`${DORSAL}/locations`);

    expect(requete.request.method).toBe('POST');

    // Le secret DOIT partir.
    // Cela rend vérifiable son absence des journaux et des spans.
    const envoye = requete.request.body as DemandeLocation;
    expect(envoye.codeStation).toBe('ST-COMEDIE');
    expect(envoye.motDePasse).toBeTruthy();
    expect(envoye.courriel).toContain('@');

    requete.flush({ reference: 'L-0001', identifiantVelo: 'V-0006' });
    expect((await promesse).reference).toBe('L-0001');
  });

  it('poste la station de retour sur la restitution', async () => {
    const promesse = firstValueFrom(api.restituer('L-0001', 'ST-ARCEAUX'));
    const requete = httpMock.expectOne(`${DORSAL}/locations/L-0001/restitution`);

    expect(requete.request.method).toBe('POST');
    expect((requete.request.body as DemandeRestitution).codeStation).toBe('ST-ARCEAUX');

    requete.flush({ reference: 'L-0001', dureeMinutes: 1, montant: 1.05 });
    expect((await promesse).montant).toBe(1.05);
  });

  it('lit le rapport sur GET /locations/{reference}/rapport', async () => {
    const promesse = firstValueFrom(api.rapport('L-0001'));
    const requete = httpMock.expectOne(`${DORSAL}/locations/L-0001/rapport`);

    expect(requete.request.method).toBe('GET');
    requete.flush({ reference: 'L-0001', codeDepart: 'ST-COMEDIE', indiceRotation: 100, locationsDepuis: 1 });
    expect((await promesse).indiceRotation).toBe(100);
  });

  it('laisse remonter un echec HTTP sans le traduire', async () => {
    const promesse = firstValueFrom(api.ouvrir('ST-PEYROU'));
    httpMock
      .expectOne(`${DORSAL}/locations`)
      .flush({ detail: 'station hors service : ST-PEYROU' },
             { status: 503, statusText: 'x' });
    //  La couche d'accès ne construit pas de message d'erreur.
    //  Elle laisse passer à parc-errors.ts qui traduit.
    await expect(promesse).rejects.toBeInstanceOf(HttpErrorResponse);
  });
});