/**
 * Démarre un service AU PREMIER PLAN.
 *
 *     node scripts/demarrer.mjs parc
 *     node scripts/demarrer.mjs locations
 *
 * <p>Tout argument supplémentaire est transmis TEL QUEL à la JVM, avant `-jar`.
 * Cela permet, par exemple, de passer une option de démarrage supplémentaire :</p>
 *
 *     node scripts/demarrer.mjs parc -Dparc.consolidation-ms=1200
 *
 * <p>Ctrl-C pour arrêter.</p>
 */
import { spawn } from 'node:child_process';

import { SERVICES, argumentsJvm, attendrePortLibre } from './lib.mjs';

const [nom, ...jvmEnPlus] = process.argv.slice(2);

if (!nom || !SERVICES[nom]) {
  console.error('usage : node scripts/demarrer.mjs <parc|locations> [arguments JVM...]');
  process.exit(2);
}

let argv;
try {
  argv = await argumentsJvm(nom, jvmEnPlus);
} catch (e) {
  console.error(e.message);
  process.exit(1);
}

const { port } = SERVICES[nom];
if (!(await attendrePortLibre(port, 5))) {
  console.error(
    `le port ${port} est deja occupe : arretez le processus qui l'utilise`,
  );
  process.exit(1);
}

console.log(`demarrage du service « ${nom} » sur le port ${port} — Ctrl-C pour arreter`);
if (jvmEnPlus.length) {
  console.log(`arguments JVM supplementaires : ${jvmEnPlus.join(' ')}`);
}

//  `stdio: 'inherit'` permet au journal du service d'apparaître directement dans le
//  terminal, et Ctrl-C l'atteint.
const p = spawn('java', argv, { stdio: 'inherit' });
p.on('error', (e) => {
  console.error(`java introuvable : ${e.message}`);
  process.exit(1);
});
p.on('exit', (code, signal) => process.exit(signal ? 0 : code ?? 0));
