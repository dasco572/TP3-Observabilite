/**
 * Relie un journal à une trace.
 *
 *     node examples/relier-journal-trace.mjs --journal mon-journal.txt
 *     node examples/relier-journal-trace.mjs --journal mon-journal.txt --trace <id>
 *
 * <p>Sans {@code --trace}, l'outil liste les identifiants de trace qu'il voit,
 * avec le nombre de lignes qui les portent.
 * Avec, il n'imprime que les lignes de cette trace.</p>
 *
 * <p>Il reconnaît les deux formes :
 * la forme console ({@code trace_id=abcd...})
 * et la forme structurée ({@code "trace_id":"abcd..."}).</p>
 *
 * <p>C'est l'autre moitié de {@code lire-trace.mjs} : celui-là montre où
 * est passé le temps, celui-ci montre ce que le programme avait à dire
 * pendant ce temps-là.</p>
 */
import fs from 'node:fs/promises';

const args = process.argv.slice(2);
const option = (nom, defaut) => {
  const i = args.indexOf(nom);
  return i !== -1 && args[i + 1] ? args[i + 1] : defaut;
};

const journal = option('--journal', null);
const trace = option('--trace', null);

if (!journal) {
  console.error('usage : node examples/relier-journal-trace.mjs --journal <fichier>');
  console.error('                                               [--trace <id>]');
  process.exit(2);
}

let texte;
try {
  texte = await fs.readFile(journal, 'utf8');
} catch (e) {
  console.error(`journal illisible : ${e.message}`);
  process.exit(1);
}

//  Les deux formes possibles, en une seule expression.
//  Le groupe nommé évite de dépendre de l ordre des alternatives.
const MOTIF = /(?:trace_id\s*[=:]\s*"?|"trace[._]id"\s*:\s*")(?<id>[0-9a-f]{32})/gi;

const lignes = texte.split(/\r?\n/);
const parTrace = new Map();

lignes.forEach((ligne, numero) => {
  for (const m of ligne.matchAll(MOTIF)) {
    const id = m.groups.id.toLowerCase();
    if (!parTrace.has(id)) parTrace.set(id, []);
    parTrace.get(id).push({ numero: numero + 1, ligne });
  }
});

if (!parTrace.size) {
  console.log(`aucun identifiant de trace dans ${journal}.`);
  console.log('');
  console.log('Deux causes possibles :');
  console.log('  - le motif de journalisation ne fait pas sortir trace_id ;');
  console.log('  - aucun agent n etait attache, et il n y avait donc rien a sortir.');
  process.exit(1);
}

if (!trace) {
  console.log(`${journal} : ${parTrace.size} trace(s), ${lignes.length} ligne(s) lues`);
  console.log('-'.repeat(72));
  const triees = [...parTrace.entries()].sort((a, b) => b[1].length - a[1].length);
  for (const [id, occurrences] of triees) {
    const premiere = occurrences[0].numero;
    console.log(
      `  ${id}  ${String(occurrences.length).padStart(3)} ligne(s)` +
        `  (a partir de la ligne ${premiere})`,
    );
  }
  console.log('-'.repeat(72));
  console.log('Pour voir les lignes d une trace : --trace <identifiant>');
  process.exit(0);
}

const cible = trace.toLowerCase();
const occurrences = parTrace.get(cible);
if (!occurrences) {
  console.log(`la trace ${cible} n apparait pas dans ${journal}.`);
  process.exit(1);
}

console.log(`trace ${cible} : ${occurrences.length} ligne(s) de ${journal}`);
console.log('-'.repeat(92));
for (const { numero, ligne } of occurrences) {
  console.log(`${String(numero).padStart(5)} | ${ligne}`);
}