/**
 * Fonctions communes aux scripts du projet de démarrage.
 *
 * <p>Tout est en Node, et uniquement avec la bibliothèque standard.
 * Les scripts tournent tels quels sous Windows, macOS et Linux, 
 * sans PowerShell, sans Bash et sans WSL.</p>
 *
 * <p>RÈGLE DE PORTABILITÉ :
 * les processus sont lancés avec un TABLEAU d'arguments.</p>
 */
import fs from 'node:fs/promises';
import net from 'node:net';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

/** Racine du projet : le dossier qui contient `scripts/`. */
export const RACINE = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  '..',
);

/** Les deux services, leur port et le JAR que Maven produit. */
export const SERVICES = {
  parc: {
    port: 8081,
    jar: path.join(RACINE, 'parc', 'target', 'parc.jar'),
    module: 'parc',
  },
  locations: {
    port: 8080,
    jar: path.join(RACINE, 'locations', 'target', 'locations.jar'),
    module: 'locations',
  },
};

export const URL_LOCATIONS = 'http://127.0.0.1:8080';
export const URL_PARC = 'http://127.0.0.1:8081';

export const existe = async (p) => fs.access(p).then(() => true, () => false);

export const dormir = (ms) => new Promise((r) => setTimeout(r, ms));

/** Teste si quelque chose écoute sur un port local. */
export function portOccupe(port) {
  return new Promise((resolve) => {
    const s = net.createConnection({ host: '127.0.0.1', port });
    const fin = (v) => {
      s.destroy();
      resolve(v);
    };
    s.setTimeout(700);
    s.once('connect', () => fin(true));
    s.once('timeout', () => fin(false));
    s.once('error', () => fin(false));
  });
}

/** Attend que le port soit réellement libre avant de démarrer une JVM. */
export async function attendrePortLibre(port, secondes = 30) {
  for (let i = 0; i < secondes * 2; i += 1) {
    if (!(await portOccupe(port))) return true;
    await dormir(500);
  }
  return false;
}

/** Attend que le port réponde. */
export async function attendrePortOuvert(port, secondes = 60) {
  for (let i = 0; i < secondes * 2; i += 1) {
    await dormir(500);
    if (await portOccupe(port)) return true;
  }
  return false;
}

/**
 * Construit la ligne de commande de la JVM d'un service.
 *
 * <p>`jvmEnPlus` est recopié TEL QUEL avant `-jar`.
 * Tout argument supplémentaire donné sur la ligne de commande passe par là.</p>
 */
export async function argumentsJvm(nom, jvmEnPlus = []) {
  const service = SERVICES[nom];
  if (!service) {
    throw new Error(`service inconnu : ${nom} (parc ou locations)`);
  }
  if (!(await existe(service.jar))) {
    throw new Error(
      `jar absent — construisez le service :\n` +
        `    mvn -f ${service.module}/pom.xml clean package`,
    );
  }
  //  UTF-8 de bout en bout, sinon la JVM écrit en Cp1252 sous Windows.
  return [
    '-Dfile.encoding=UTF-8',
    '-Dsun.stdout.encoding=UTF-8',
    '-Dsun.stderr.encoding=UTF-8',
    ...jvmEnPlus,
    '-jar',
    service.jar,
  ];
}

/** Appelle une route et rend le statut et le corps. */
export async function appeler(url, { methode = 'GET', corps } = {}) {
  const reponse = await fetch(url, {
    method: methode,
    headers: corps ? { 'Content-Type': 'application/json' } : undefined,
    body: corps ? JSON.stringify(corps) : undefined,
    signal: AbortSignal.timeout(20000),
  });
  const texte = await reponse.text();
  let json = null;
  try {
    json = texte ? JSON.parse(texte) : null;
  } catch {
    json = null;
  }
  return { statut: reponse.status, corps: json, texte };
}
