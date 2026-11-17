/**
 * L'état de l'application, testé sans réseau.
 *
 * <p>Le magasin ne dépend que de {@link ParcApi}.
 * Un double de quelques lignes suffit donc à le piloter,
 * y compris dans ses cas d'échec.</p>
 */
import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { ParcApi } from './parc-api';
import { ParcStore } from './parc-store';
import { Location, Station } from './parc-models';

const STATIONS: Station[] = [
  {
    code: 'ST-COMEDIE',
    nom: 'Place de la Comédie',
    capacite: 20,
    velosDisponibles: 7,
    horsService: false,
    louable: true,
  },
  {
    code: 'ST-PEYROU',
    nom: 'Promenade du Peyrou',
    capacite: 8,
    velosDisponibles: 5,
    horsService: true,
    louable: false,
  },
];

const EN_COURS: Location = {
  reference: 'L-0001',
  identifiantVelo: 'V-0006',
  codeDepart: 'ST-COMEDIE',
  codeArrivee: null,
  dureeMinutes: 0,
  montant: null,
  etat: 'EN_COURS',
};

const CLOTUREE: Location = {
  ...EN_COURS,
  codeArrivee: 'ST-ARCEAUX',
  dureeMinutes: 1,
  montant: 1.05,
  etat: 'CLOTUREE',
};

function configurer(api: Partial<ParcApi>) {
  TestBed.configureTestingModule({
    providers: [{ provide: ParcApi, useValue: api }],
  });
  return TestBed.inject(ParcStore);
}

describe('ParcStore', () => {
  it('part de l etat vide', () => {
    const store = configurer({});
    expect(store.resultat().etat).toBe('vide');
    expect(store.stations()).toEqual([]);
    expect(store.locations()).toEqual([]);
    expect(store.rapport()).toBeNull();
    expect(store.enCours()).toBe(false);
  });

  it('charge les stations et les locations, et compte les louables', async () => {
    const store = configurer({
      stations: () => of(STATIONS),
      locations: () => of([EN_COURS]),
    });

    await store.rafraichir();

    expect(store.stations()).toEqual(STATIONS);
    expect(store.locations()).toEqual([EN_COURS]);
    expect(store.resultat()).toEqual({
      etat: 'succes',
      message: '2 stations, dont 1 louables',
    });
    expect(store.enCours()).toBe(false);
  });

  it('ne retient comme en cours que les locations non cloturees', async () => {
    const store = configurer({
      stations: () => of(STATIONS),
      locations: () => of([EN_COURS, CLOTUREE]),
    });

    await store.rafraichir();

    expect(store.locationsEnCours()).toEqual([EN_COURS]);
  });

  it('annonce la reference et le velo apres une location', async () => {
    const store = configurer({
      stations: () => of(STATIONS),
      locations: () => of([EN_COURS]),
      ouvrir: () => of(EN_COURS),
    });

    await store.louer('ST-COMEDIE');

    const resultat = store.resultat();
    expect(resultat.etat).toBe('succes');
    if (resultat.etat === 'succes') {
      expect(resultat.message).toContain('L-0001');
      expect(resultat.message).toContain('V-0006');
    }
  });

  it('annonce la duree et le montant apres une restitution', async () => {
    const store = configurer({
      stations: () => of(STATIONS),
      locations: () => of([CLOTUREE]),
      restituer: () => of(CLOTUREE),
    });

    await store.rendre('L-0001', 'ST-ARCEAUX');

    const resultat = store.resultat();
    if (resultat.etat === 'succes') {
      expect(resultat.message).toContain('1 min');
      expect(resultat.message).toContain('1.05');
    } else {
      throw new Error('la restitution devait reussir');
    }
  });

  it('publie le rapport recu', async () => {
    const store = configurer({
      rapport: () =>
        of({
          reference: 'L-0001',
          codeDepart: 'ST-COMEDIE',
          indiceRotation: 100,
          locationsDepuis: 2,
        }),
    });

    await store.voirRapport('L-0001');

    expect(store.rapport()?.indiceRotation).toBe(100);
  });

  it('traduit un refus du service en echec lisible', async () => {
    const store = configurer({
      ouvrir: () =>
        throwError(
          () =>
            new HttpErrorResponse({
              status: 503,
              error: { detail: 'station hors service : ST-PEYROU' },
            }),
        ),
    });

    await store.louer('ST-PEYROU');

    const resultat = store.resultat();
    expect(resultat.etat).toBe('echec');
    if (resultat.etat === 'echec') {
      expect(resultat.echec.categorie).toBe('refus-du-service');
      expect(resultat.echec.statut).toBe(503);
      expect(resultat.echec.message).toContain('hors service');
    }
    expect(store.enCours()).toBe(false);
  });

  it('distingue un service injoignable d un refus', async () => {
    const store = configurer({
      stations: () => throwError(() => new HttpErrorResponse({ status: 0 })),
    });

    await store.rafraichir();

    const resultat = store.resultat();
    if (resultat.etat === 'echec') {
      expect(resultat.echec.categorie).toBe('injoignable');
    } else {
      throw new Error('un service arrete devait produire un echec');
    }
  });

  it('rend toujours la main, meme en echec', async () => {
    const store = configurer({
      rapport: () => throwError(() => new HttpErrorResponse({ status: 500 })),
    });

    await store.voirRapport('L-0001');

    expect(store.enCours()).toBe(false);
  });
});