# TP 3 — Scénarios et données de validation

Ce dossier contient de quoi **EXERCER** l'application cible et **VÉRIFIER** qu'elle fonctionne encore.

**Prérequis :** Node.js (version 20.19, 22.12 ou 24 et plus).  
Aucune dépendance à installer, vu que tout n'utilise que la bibliothèque standard de Node.

**Où extraire, et depuis où lancer.** Cette archive porte la racine
`velos-tp3/` : extrayez-la **dans le même dossier parent** que `starter.zip`
et `examples.zip`, et son contenu se place de lui-même en
`velos-tp3/resources/`. **Toutes** les commandes ci-dessous se lancent depuis
la racine `velos-tp3/`.

---

## 1. Les quatre outils

- `node resources/scenarios.mjs`  
  Joue les **SEPT** scénarios fonctionnels du TP, toujours dans le même ordre, et affiche leur statut et leur durée. Chaque scénario a un but d'observation ; ce qu'il faut en observer, c'est à vous de le décider.

- `node resources/verifier-fonctionnel.mjs`  
  21 contrôles du socle **FONCTIONNEL** : le parc de départ, ce que l'interface web voit, le cycle ouvrir / consulter / restituer, et les six refus. À lancer sur deux services **FRAÎCHEMENT** démarrés.  
  C'est le contrôle du point de contrôle A0 et c'est aussi à relancer après avoir instrumenté pour vérifier que l'instrumentation n'a pas modifié le comportement.

- `node resources/verifier-confidentialite.mjs`  
  La **SENTINELLE**. Elle cherche le mot de passe et l'adresse complète de l'abonné de démonstration dans les fichiers de journal que vous lui désignez, et dans les spans stockés par Jaeger.  
  Elle échoue dès la première occurrence.
  - `--journaux <dossier>` : où chercher vos journaux (défaut : `journaux`)
  - `--jaeger <url>` : où joindre Jaeger (défaut : `localhost:16686`)

- `node resources/telecharger-agent.mjs`  
  Récupère l'agent Java OpenTelemetry depuis Maven Central (version 2.32.0 par défaut). Relancer le script ne retélécharge rien.
  - `--version <x.y.z>` : une autre version
  - `--vers <dossier>` : où le déposer (défaut : le dossier courant)

Sous macOS, Linux et Git Bash, chaque outil a son enveloppe `.sh` :
```bash
sh resources/scenarios.sh

```

---

## 2. L'abonné de démonstration

Les scénarios envoient **VOLONTAIREMENT** une adresse électronique complète et un mot de passe.
Ce sont des valeurs indicatives ne désignant personne.

Elles sont définies **UNE SEULE FOIS**, dans `resources/lib.mjs`, parce que deux outils en dépendent : celui qui les envoie et celui qui vérifie qu'elles ne ressortent nulle part.

On ne démontre pas qu'un secret ne fuite pas en s'abstenant de l'envoyer.

Le **DOMAINE** de l'adresse reste autorisé dans votre télémétrie et répond à la question posée sans singulariser personne.
La sentinelle le compte sans le reprocher.

---

## 3. Ordre de lancement

```bash
# terminal 1
node scripts/demarrer.mjs parc            # port 8081

# terminal 2
node scripts/demarrer.mjs locations        # port 8080

# terminal 3
node resources/verifier-fonctionnel.mjs

```

Le service des locations appelle celui du parc.
Les deux outils de vérification s'appuient sur l'état **INITIAL** du parc.
Pour les rejouer, arrêtez les deux services et redémarrez-les.
Les bases sont en mémoire et récréées à chaque démarrage.

---

## 4. Ce que ce dossier ne contient pas
Aucun point d'instrumentation, aucun nom de span, aucun champ de journal structuré, aucune valeur de trace, aucun diagnostic.
Décider où observer, quoi émettre et comment, c'est le travail du TP.
Les valeurs fonctionnelles attendues sont dans `ATTENDU.md`.