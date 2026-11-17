/**
 * Sentinelle de confidentialité.
 *
 * <p>L'interface web et les scénarios envoient <b>volontairement</b> un mot
 * de passe et une adresse électronique complète. Cet outil vérifie qu'ils ne
 * ressortent dans <b>aucune</b> sortie que vous produisez :</p>
 *
 * <ul>
 *   <li>les fichiers de journal que vous lui désignez ;</li>
 *   <li>les spans stockés par Jaeger, donc tout ce qui a traversé le
 *       Collector, télémétrie du navigateur comprise.</li>
 * </ul>
 *
 *     node resources/verifier-confidentialite.mjs
 *     node resources/verifier-confidentialite.mjs --journaux mes-journaux
 *     node resources/verifier-confidentialite.mjs --jaeger http://localhost:16686
 *
 * <p>Le domaine de l'adresse est <b>autorisé</b> :
 * il répond à la question posée sans singulariser personne.
 * L'outil le compte, sans le reprocher.</p>
 *
 * <p>C'est le seul contrôle du TP qui doit échouer bruyamment parce qu'un secret
 * dans la télémétrie c'est un incident.</p>
 */
import fs from 'node:fs/promises';
import path from 'node:path';

import {
  ABONNE, CHAMPS_INTERDITS, DOMAINE, FRAGMENT_SECRET, SECRET, URL_JAEGER, compter,
} from './lib.mjs';

const args = process.argv.slice(2);
const option = (nom, defaut) => {
  const i = args.indexOf(nom);
  return i !== -1 && args[i + 1] ? args[i + 1] : defaut;
};

const dossierJournaux = option('--journaux', 'journaux');
const jaeger = option('--jaeger', URL_JAEGER);

const EXTENSIONS = ['.txt', '.log', '.json', '.jsonl', '.ndjson', '.out'];
const isoDecale = (minutes) =>
  new Date(Date.now() + minutes * 60_000).toISOString().replace(/\.\d+Z$/, 'Z');

/**
 * Compte, dans un texte, tout ce qui ne doit pas y être.
 *
 * <p>Trois marqueurs sont détectés :</p>
 * <ul>
 *   <li>le mot de passe ENTIER ;</li>
 *   <li>un FRAGMENT du mot de passe, parce qu un cadriciel peut l'avoir
 *       tronqué. Par exemple, Spring Web coupe à une centaine de caractères ce qu'il
 *       imprime d'un corps de requête en DEBUG ;</li>
 *   <li>un NOM DE CHAMP interdit, parce qu'une ligne qui contient
 *       « motDePasse » a journalisé un objet entier sans regarder ce qu'il
 *       portait.</li>
 * </ul>
 *
 * <p>L'adresse complète est comptée à part.
 * Le domaine seul est compté aussi, mais il est autorisé.</p>
 */
function examiner(texte) {
  const champs = CHAMPS_INTERDITS.reduce((t, c) => t + compter(texte, c), 0);
  return {
    secret: compter(texte, SECRET),
    fragment: compter(texte, FRAGMENT_SECRET),
    champs,
    adresse: compter(texte, ABONNE),
    domaine: compter(texte, DOMAINE),
  };
}

/** Ce qui constitue une fuite. Le fragment englobe le secret entier. */
const fuitesDe = (c) => c.fragment + c.champs + c.adresse;

const ligne = (quoi, c) =>
  `  ${quoi.padEnd(40)} secret=${c.secret}  fragment=${c.fragment}  ` +
  `champ=${c.champs}  adresse=${c.adresse}  domaine_seul=${c.domaine}`;

let fuites = 0;
let inspectes = 0;

/** Tous les fichiers texte d'un dossier, récursivement. */
async function fichiersTexte(racine) {
  const trouves = [];
  async function descendre(dossier) {
    let entrees;
    try {
      entrees = await fs.readdir(dossier, { withFileTypes: true });
    } catch {
      return;
    }
    for (const e of entrees) {
      const complet = path.join(dossier, e.name);
      if (e.isDirectory()) {
        if (e.name !== 'node_modules' && e.name !== 'target') await descendre(complet);
      } else if (EXTENSIONS.includes(path.extname(e.name).toLowerCase())) {
        trouves.push(complet);
      }
    }
  }
  await descendre(racine);
  return trouves.sort();
}

console.log(`=== 1. fichiers de journal sous « ${dossierJournaux} » ===`);
const fichiers = await fichiersTexte(dossierJournaux);
if (!fichiers.length) {
  console.log('  (aucun fichier texte trouvé : enregistrez vos journaux, ou');
  console.log('   indiquez le bon dossier avec --journaux <dossier>)');
}
for (const f of fichiers) {
  const compte = examiner(await fs.readFile(f, 'utf8'));
  //  Chemin relatif quand il reste lisible, absolu sinon.
  const relatif = path.relative('.', f);
  console.log(ligne(relatif.startsWith('..') ? f : relatif, compte));
  fuites += fuitesDe(compte);
  inspectes += 1;
}

console.log('');
console.log('=== 2. spans stockés par Jaeger ===');
let services = [];
try {
  const r = await fetch(`${jaeger}/api/v3/services`, { signal: AbortSignal.timeout(5000) });
  services = (await r.json()).services ?? [];
} catch {
  console.log(`  (Jaeger injoignable sur ${jaeger} : contrôle limite aux fichiers)`);
}

for (const svc of services) {
  if (svc === 'jaeger') continue;
  //  On inspecte la réponse BRUTE, et non des champs choisis : un secret
  //  peut se cacher dans un attribut qu'on n'aurait pas pensé à lire.
  const url =
    `${jaeger}/api/v3/traces?query.service_name=${encodeURIComponent(svc)}` +
    `&query.num_traces=100&query.start_time_min=${isoDecale(-180)}` +
    `&query.start_time_max=${isoDecale(5)}`;
  let brut = '';
  try {
    brut = await (await fetch(url, { signal: AbortSignal.timeout(10000) })).text();
  } catch {
    console.log(`  (service « ${svc} » illisible)`);
    continue;
  }
  const compte = examiner(brut);
  console.log(ligne(`service « ${svc} »`, compte));
  fuites += fuitesDe(compte);
  inspectes += 1;
}

console.log('');
if (inspectes === 0) {
  console.log('AUCUNE SOURCE INSPECTÉE : ce résultat ne prouve rien.');
  console.log('Produisez des journaux, démarrez Jaeger, puis relancez.');
  process.exit(2);
}
if (fuites > 0) {
  console.log(`ECHEC : ${fuites} occurrence(s) de donnée sensible dans votre télémetrie.`);
  console.log('Un mot de passe ou une adresse complète dans un journal ou dans un');
  console.log('span est un incident. Trouvez la ligne fautive et retirez-la.');
  process.exit(1);
}
console.log(`OK : ${inspectes} source(s) inspectée(s). Aucun mot de passe, même`);
console.log('     tronqué, aucun nom de champ interdit, aucune adresse complète.');
console.log("     Seul le domaine subsiste, ce qui était l'intention.");
