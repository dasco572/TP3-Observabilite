/**
 * Les sept scénarios fonctionnels du TP 3.
 *
 * <p>Ils s'exécutent toujours dans le même ordre sur un parc fraîchement
 * démarré, et produisent toujours les mêmes résultats.</p>
 *
 *     node resources/scenarios.mjs
 *
 * <p>Chaque scénario a un <b>but d'observation</b>, rappelé dans la colonne
 * de droite. Ce que l'on doit voir dans les journaux et dans les traces,
 * c'est à vous de le décider.</p>
 */
import { ABONNE, SECRET, URL_LOCATIONS, appeler, exigerLesServices } from './lib.mjs';

await exigerLesServices();

const abonne = { courriel: ABONNE, motDePasse: SECRET };

/**
 * La référence de la location ouverte au scénario 2, réutilisée par les
 * scénarios 6 et 7.
 * Les sept scénarios s'enchaînent donc même si des locations existaient déjà.
 */
let reference = 'L-0001';

const SCENARIOS = [
  {
    nom: '1. parcours nominal',
    but: 'le cas qui marche, de bout en bout',
    joue: () => appeler(`${URL_LOCATIONS}/stations`),
  },
  {
    nom: '2. ouverture de location',
    but: 'un jalon métier que personne ne devine',
    joue: async () => {
      const r = await appeler(`${URL_LOCATIONS}/locations`, {
        methode: 'POST',
        corps: { codeStation: 'ST-COMEDIE', ...abonne },
      });
      if (r.corps?.reference) reference = r.corps.reference;
      return r;
    },
  },
  {
    nom: '3. station sans vélo',
    but: 'un refus, et non une panne',
    joue: () =>
      appeler(`${URL_LOCATIONS}/locations`, {
        methode: 'POST',
        corps: { codeStation: 'ST-GARE', ...abonne },
      }),
  },
  {
    nom: '4. station hors service',
    but: "un échec provoqué par L'AUTRE service",
    joue: () =>
      appeler(`${URL_LOCATIONS}/locations`, {
        methode: 'POST',
        corps: { codeStation: 'ST-PEYROU', ...abonne },
      }),
  },
  {
    nom: '5. abonné refusé',
    but: 'un secret envoyé pour de bon',
    joue: () =>
      appeler(`${URL_LOCATIONS}/locations`, {
        methode: 'POST',
        corps: { codeStation: 'ST-COMEDIE', courriel: ABONNE, motDePasse: 'faux' },
      }),
  },
  {
    nom: "6. rapport d'exploitation",
    but: "l'opération lente : où passe le temps ?",
    joue: () => appeler(`${URL_LOCATIONS}/locations/${reference}/rapport`),
  },
  {
    nom: '7. restitution',
    but: 'la clôture, sa durée et son montant',
    joue: () =>
      appeler(`${URL_LOCATIONS}/locations/${reference}/restitution`, {
        methode: 'POST',
        corps: { codeStation: 'ST-ARCEAUX' },
      }),
  },
];

console.log("scénario                        statut    durée  but d'observation");
console.log('-'.repeat(96));

const resultats = [];
for (const scenario of SCENARIOS) {
  const t0 = process.hrtime.bigint();
  let statut = 0;
  try {
    statut = (await scenario.joue()).statut;
  } catch (e) {
    console.error(`  ${scenario.nom} : ${e.message}`);
  }
  const ms = Number(process.hrtime.bigint() - t0) / 1e6;
  resultats.push({ ...scenario, statut, ms });
  console.log(
    `${scenario.nom.padEnd(32)}${String(statut).padEnd(10)}` +
      `${ms.toFixed(0).padStart(5)} ms  ${scenario.but}`,
  );
}

console.log('');
const lent = resultats.reduce((a, b) => (a.ms > b.ms ? a : b));
console.log(`le scénario le plus long est « ${lent.nom} » : ${lent.ms.toFixed(0)} ms.`);
console.log("Rien ici ne dit POURQUOI. C'est la question du TP.");
