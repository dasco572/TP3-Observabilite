/**
 * Télécharge l'agent Java OpenTelemetry.
 *
 * <p>L'agent n'est pas livré avec les ressources.
 * C'est un binaire d'une vingtaine de mégaoctets,
 * et la bonne pratique est de le récupérer depuis Maven Central une seule fois.</p>
 *
 *     node resources/telecharger-agent.mjs
 *     node resources/telecharger-agent.mjs --version 2.32.0
 *     node resources/telecharger-agent.mjs --vers mon-dossier
 *
 * <p>Le fichier est d'abord écrit sous un nom temporaire puis renommé.
 * Un téléchargement interrompu ne laisse donc jamais un agent tronqué.</p>
 */
import { createWriteStream } from 'node:fs';
import fs from 'node:fs/promises';
import path from 'node:path';
import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';

/** Version épinglée. */
const VERSION_PAR_DEFAUT = '2.32.0';

const args = process.argv.slice(2);
const option = (nom, defaut) => {
  const i = args.indexOf(nom);
  return i !== -1 && args[i + 1] ? args[i + 1] : defaut;
};

const version = option('--version', VERSION_PAR_DEFAUT);
const dossier = path.resolve(option('--vers', '.'));
const cible = path.join(dossier, 'opentelemetry-javaagent.jar');
const mo = (o) => (o / 1024 / 1024).toFixed(1);

const existe = await fs.access(cible).then(() => true, () => false);
if (existe) {
  const { size } = await fs.stat(cible);
  console.log(`agent deja present (${mo(size)} Mo) : ${cible}`);
  process.exit(0);
}

const url =
  'https://repo1.maven.org/maven2/io/opentelemetry/javaagent/' +
  `opentelemetry-javaagent/${version}/opentelemetry-javaagent-${version}.jar`;

console.log(`telechargement de l agent ${version} ...`);
console.log(`  depuis ${url}`);

const r = await fetch(url);
if (!r.ok) {
  console.error(`ECHEC : Maven Central a repondu ${r.status} ${r.statusText}`);
  console.error('  verifiez la version demandee et l acces reseau.');
  process.exit(1);
}

const attendu = Number(r.headers.get('content-length') ?? 0);
const partiel = `${cible}.partiel`;
await fs.mkdir(dossier, { recursive: true });
await pipeline(Readable.fromWeb(r.body), createWriteStream(partiel));

const { size } = await fs.stat(partiel);
if (attendu && size !== attendu) {
  await fs.rm(partiel, { force: true });
  console.error(`ECHEC : ${size} octets recus, ${attendu} annonces.`);
  process.exit(1);
}

await fs.rename(partiel, cible);
console.log(`agent ${version} telecharge (${mo(size)} Mo) : ${cible}`);
