/**
 * Contrôle du socle fonctionnel : 
 * l'application fait-elle encore ce qu'elle faisait ?
 *
 *     node resources/verifier-fonctionnel.mjs
 *
 * <p>À lancer sur deux services FRAÎCHEMENT démarrés.
 * Il s'appuie sur l'état initial du parc, et ouvre la location {@code L-0001}.</p>
 *
 * <p>C'est le contrôle du point de contrôle A0, et c'est à relancer aussi après avoir instrumenté :
 * <b>instrumenter ne doit rien changer au comportement</b>.
 * Il ne vérifie rien de la télémétrie.</p>
 */
import { ABONNE, SECRET, URL_LOCATIONS, URL_PARC, appeler, exigerLesServices } from './lib.mjs';

await exigerLesServices();

const abonne = { courriel: ABONNE, motDePasse: SECRET };
let echecs = 0;

function verifier(quoi, obtenu, attendu) {
  const ok = JSON.stringify(obtenu) === JSON.stringify(attendu);
  console.log(
    `  ${ok ? 'OK  ' : 'ECHEC'} ${quoi.padEnd(52)} ` +
      `${ok ? '' : `obtenu ${JSON.stringify(obtenu)}, attendu ${JSON.stringify(attendu)}`}`,
  );
  if (!ok) echecs += 1;
}

console.log('=== 1. le parc de départ ===');
const stations = (await appeler(`${URL_PARC}/stations`)).corps;
verifier('nombre de stations', stations.length, 6);
verifier(
  'codes des stations',
  stations.map((s) => s.code),
  ['ST-ARCEAUX', 'ST-COMEDIE', 'ST-GARE', 'ST-MOSSON', 'ST-PEYROU', 'ST-TRIOLET'],
);
verifier(
  'vélos retirables par station',
  stations.map((s) => s.velosDisponibles),
  [4, 7, 0, 3, 5, 2],
);
verifier(
  'total des vélos retirables',
  stations.reduce((t, s) => t + s.velosDisponibles, 0),
  21,
);
verifier(
  'stations hors service',
  stations.filter((s) => s.horsService).map((s) => s.code),
  ['ST-PEYROU'],
);

console.log('');
console.log('=== 2. ce que l interface web voit ===');
const vues = (await appeler(`${URL_LOCATIONS}/stations`)).corps;
verifier('nombre de stations vues du frontal', vues.length, 6);
verifier(
  'stations louables',
  vues.filter((s) => s.louable).map((s) => s.code),
  ['ST-ARCEAUX', 'ST-COMEDIE', 'ST-MOSSON', 'ST-TRIOLET'],
);

console.log('');
console.log('=== 3. ouvrir, consulter, restituer ===');
const ouverte = await appeler(`${URL_LOCATIONS}/locations`, {
  methode: 'POST',
  corps: { codeStation: 'ST-COMEDIE', ...abonne },
});
verifier('ouverture : statut', ouverte.statut, 201);
verifier('ouverture : référence', ouverte.corps?.reference, 'L-0001');
verifier('ouverture : vélo retiré', ouverte.corps?.identifiantVelo, 'V-0006');
verifier('ouverture : état', ouverte.corps?.etat, 'EN_COURS');

const rapport = await appeler(`${URL_LOCATIONS}/locations/L-0001/rapport`);
verifier('rapport : statut', rapport.statut, 200);
verifier('rapport : indice de rotation', rapport.corps?.indiceRotation, 100);
verifier('rapport : locations à cette station', rapport.corps?.locationsDepuis, 1);

const rendue = await appeler(`${URL_LOCATIONS}/locations/L-0001/restitution`, {
  methode: 'POST',
  corps: { codeStation: 'ST-ARCEAUX' },
});
verifier('restitution : statut', rendue.statut, 200);
verifier('restitution : durée facturée', rendue.corps?.dureeMinutes, 1);
verifier('restitution : montant', rendue.corps?.montant, 1.05);
verifier('restitution : état', rendue.corps?.etat, 'CLOTUREE');

console.log('');
console.log('=== 4. les refus ===');
const vide = await appeler(`${URL_LOCATIONS}/locations`, {
  methode: 'POST',
  corps: { codeStation: 'ST-GARE', ...abonne },
});
verifier('station sans vélo : statut', vide.statut, 409);

const horsService = await appeler(`${URL_LOCATIONS}/locations`, {
  methode: 'POST',
  corps: { codeStation: 'ST-PEYROU', ...abonne },
});
verifier('station hors service : statut', horsService.statut, 503);

const inconnue = await appeler(`${URL_LOCATIONS}/locations`, {
  methode: 'POST',
  corps: { codeStation: 'ST-AILLEURS', ...abonne },
});
verifier('station inconnue : statut', inconnue.statut, 404);

const refuse = await appeler(`${URL_LOCATIONS}/locations`, {
  methode: 'POST',
  corps: { codeStation: 'ST-COMEDIE', courriel: ABONNE, motDePasse: 'faux' },
});
verifier('mot de passe faux : statut', refuse.statut, 401);
verifier(
  'mot de passe faux : le message ne reprend pas le secret',
  refuse.texte.includes(SECRET),
  false,
);

const deuxFois = await appeler(`${URL_LOCATIONS}/locations/L-0001/restitution`, {
  methode: 'POST',
  corps: { codeStation: 'ST-ARCEAUX' },
});
verifier('restitution déjà faite : statut', deuxFois.statut, 409);

console.log('');
if (echecs > 0) {
  console.log(`ECHEC : ${echecs} contrôle(s) en défaut.`);
  process.exit(1);
}
console.log('OK : le socle fonctionnel est conforme (21 contrôles).');
