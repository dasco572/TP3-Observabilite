/**
 * Constantes et fonctions communes aux outils de validation du TP 3.
 *
 * <p>Tout est en Node, avec la seule bibliothèque standard : les outils
 * tournent tels quels sous Windows, macOS et Linux, sans PowerShell, sans
 * Bash et sans WSL.</p>
 */

export const URL_LOCATIONS = 'http://127.0.0.1:8080';
export const URL_PARC = 'http://127.0.0.1:8081';
export const URL_JAEGER = 'http://localhost:16686';

/**
 * L'abonné de démonstration, son mot de passe, et le domaine de son adresse.
 *
 * <p>Ces valeurs sont <b>indicatives</b>.
 * Elles sont définies ici, à un seul endroit, parce que deux outils en
 * dépendent — celui qui les envoie et celui qui vérifie qu'elles ne
 * ressortent nulle part.</p>
 */
export const ABONNE = 'camille.rey@etu.umontpellier.fr';
export const SECRET = 'Zx9!Vole-Tandem';
export const DOMAINE = 'etu.umontpellier.fr';

/**
 * Un FRAGMENT du mot de passe, et non le mot de passe entier.
 *
 * <p>Pourquoi : plusieurs cadriciels TRONQUENT ce qu'ils journalisent.
 * Spring Web, par exemple, coupe à une centaine de caractères la
 * représentation d'un corps de requête qu'il imprime en DEBUG. Un secret
 * ainsi coupé ne contient plus la chaîne complète, et une sentinelle qui ne
 * chercherait que celle-ci le déclarerait absent. Il est pourtant là, et
 * c'est bien une fuite.</p>
 */
export const FRAGMENT_SECRET = SECRET.slice(0, 8);

/**
 * Les noms de champ qui n'ont rien à faire dans une sortie.
 *
 * <p>Une ligne qui contient « motDePasse » a journalisé un objet entier
 * sans se demander ce qu'il portait. Le secret y est, tronqué ou non.</p>
 */
export const CHAMPS_INTERDITS = ['motDePasse', 'mot_de_passe', 'password'];

/** Appelle une route et rend le statut, le corps analysé et le texte brut. */
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

/** Compte les occurrences littérales d'une chaîne. */
export function compter(texte, aiguille) {
  if (!aiguille) return 0;
  let n = 0;
  let i = texte.indexOf(aiguille);
  while (i !== -1) {
    n += 1;
    i = texte.indexOf(aiguille, i + aiguille.length);
  }
  return n;
}

/** Vrai si les deux services répondent. */
export async function servicesPrets() {
  for (const url of [URL_LOCATIONS + '/stations', URL_PARC + '/stations']) {
    try {
      const r = await appeler(url);
      if (r.statut !== 200) return false;
    } catch {
      return false;
    }
  }
  return true;
}

/** Interrompt l'outil avec un message utile quand les services sont absents. */
export async function exigerLesServices() {
  if (!(await servicesPrets())) {
    console.error('Les deux services doivent etre demarres :');
    console.error('    node scripts/demarrer.mjs parc        (port 8081)');
    console.error('    node scripts/demarrer.mjs locations   (port 8080)');
    process.exit(1);
  }
}

export const lu = (n) => String(n).padStart(3, ' ');
