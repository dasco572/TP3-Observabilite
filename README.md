# Vélos en libre-service — projet de démarrage du TP 3
Une application **qui fonctionne** et que **vous n'avez pas écrite**.
Votre travail est de la rendre **observable**, afin de répondre à des questions d'exploitation précises.

---

## 0. Installer : les trois archives ne font qu'un seul projet

Moodle distribue **trois** archives, et elles portent **la même racine**.
Extrayez-les l'une après l'autre **dans le même dossier parent** :

```
starter.zip      ->  velos-tp3/
resources.zip    ->  velos-tp3/resources/
examples.zip     ->  velos-tp3/examples/
```

Vous obtenez **une seule arborescence** :

```
velos-tp3/
|-- parc/            locations/      frontend/     (starter.zip)
|-- scripts/         package.json    README.md     (starter.zip)
|-- resources/       scénarios, contrôles, sentinelle, agent   (resources.zip)
`-- examples/        micro-exemple Java et les deux lecteurs   (examples.zip)
```

Rien n'est à déplacer ni à renommer ensuite.

**Toutes** les commandes de ce README, du sujet et des deux autres README se lancent depuis la racine `velos-tp3/`, sauf lorsqu'elles disent explicitement de changer de répertoire (`cd frontend`).
Sous Windows, extrayez dans un dossier court comme `C:\tp3` (voir §10).

---

## 1. Ce que l'application fait

Un parc de vélos en libre-service.
On consulte les stations, on retire un vélo, on le rend ailleurs, et on consulte un rapport d'exploitation.

Trois processus coopèrent :

| Processus   | Port | Rôle                                                                                            |
| ----------- | ---- | ----------------------------------------------------------------------------------------------- |
| `frontend`  | 4200 | l'interface web (Angular) ; elle ne parle qu'au service des locations                           |
| `locations` | 8080 | le registre des locations, l'abonnement, la facturation ; **seul point d'entrée** du navigateur |
| `parc`      | 8081 | les stations et les vélos ; appelé **uniquement** par `locations`                               |

```
navigateur  ──►  locations  ──►  parc  ──►  H2
  (4200)          (8080)        (8081)
```

Chaque service a sa propre base H2 **en mémoire**, recréée à chaque démarrage.

---

## 2. Architecture

Les deux services sont organisés de la même façon, et le sens des dépendances ne s'inverse jamais.

```
api/          le bord HTTP qui convertit une requête en appel de service.
              Ne calcule rien, ne décide rien, ne traduit aucune erreur.
api/vue/      ce qui entre et ce qui sort, en types dédiés.
service/      les règles du métier qui rend un objet du métier,
              ou lève une faute du métier.
              Ignore qu'il existe un protocole HTTP.
domaine/      ce que le métier manipule. Aucune annotation technique.
depot/        ce que le métier attend du stockage (une interface).
depot/jpa/    la seule implémentation qui connaisse une base de données.
configuration/ le câblage : horloge, client HTTP, CORS, données de départ.
```

Le service des locations a une couche `parc/` de plus définissant le **port** vers l'autre service.
`ParcDistant` est une interface ; `ClientParcHttp` en est la seule implémentation.
C'est pour cette raison que `ServiceLocationsTest` s'exécute sans Spring, sans base et sans réseau.

Le frontal suit la même discipline :

```
core/configuration.ts   l'unique endroit qui connaisse une URL
parc/parc-models.ts     des types, sans comportement
parc/parc-api.ts        les routes et les charges utiles
parc/parc-errors.ts     la traduction des échecs en trois catégories
parc/parc-store.ts      l'état et les quatre gestes de l'utilisateur
parc/*-list, parc-page  l'affichage ; aucun composant n'appelle l'API
```

---

## 3. Ce qui manque (volontairement)

L'application **n'émet aucune télémétrie applicative** :

- pas une seule ligne de journalisation, dans aucune des deux applications
  dorsales ni dans le frontal ;
- aucun span écrit à la main, aucune annotation d'instrumentation ;
- aucune dépendance OpenTelemetry dans les `pom.xml` ni dans le
  `package.json` ;
- aucun Collector, aucun Jaeger, aucun `docker-compose.yml`.

Lancez les services et exercez-les ; vous verrez que la sortie reste vide.
C'est le point de départ du TP.

**Décider où observer, quoi émettre et comment, c'est le travail demandé.**
Le sujet dit ce qui est attendu.

---

## 4. Versions

| Composant   | Version testée                           |
| ----------- | ---------------------------------------- |
| JDK         | **17.0.10** (testé aussi sur **21.0.5**) |
| Maven       | **3.9.6**                                |
| Spring Boot | **4.1.1** (Spring Framework 7)           |
| H2          | **2.4.240**, en mémoire                  |
| Node        | **22.18.0** · npm **10.9.3**             |
| Angular     | **21.2** · TypeScript **5.9**            |
| Vitest      | **4.1.11** · jsdom **29.1.1**            |

Java 17 est le minimum.
Node doit satisfaire `^20.19.0 || ^22.12.0 || ^24.0.0` (exigence d'Angular 21).

---

## 5. Construire

Depuis la racine `velos-tp3/` :

```
mvn -f parc/pom.xml clean package
mvn -f locations/pom.xml clean package
cd frontend && npm ci && cd ..
```

`npm ci` **et non** `npm install`.
`npm ci` reconstruit exactement l'arbre décrit par `package-lock.json`.

---

## 6. Démarrer

Il faut respecter l'ordre suivant car le service des locations appelle celui du parc.

```
# terminal 1 — le parc
node scripts/demarrer.mjs parc

# terminal 2 — les locations
node scripts/demarrer.mjs locations

# terminal 3 — l'interface web
cd frontend && npm start
```

Puis **http://localhost:4200**. `Ctrl-C` arrête chaque processus.

Tout argument supplémentaire passé à `demarrer.mjs` est transmis **tel quel** à la JVM, avant `-jar` :

```
node scripts/demarrer.mjs parc -Dparc.consolidation-ms=1200
```

Sous macOS, Linux et Git Bash, `sh scripts/demarrer.sh parc` fait la même chose.

---

## 7. Exercer l'application

Les scénarios et les contrôles viennent de la ressource **« TP3 — Scénarios et données de validation »** ; une fois `resources.zip` extraite (voir §0), ils sont dans `resources/`, et tout se lance depuis la racine :

```
node resources/scenarios.mjs              les sept scenarios, et leurs durées
node resources/verifier-fonctionnel.mjs   21 contrôles du socle fonctionnel
node resources/verifier-confidentialite.mjs   la sentinelle
```

`verifier-fonctionnel.mjs` s'appuie sur l'état **initial** du parc, donc lancez-le sur deux services fraîchement démarrés.
C'est aussi le contrôle à relancer **après** avoir instrumenté : instrumenter ne doit rien changer au comportement.
Les valeurs attendues sont dans `resources/ATTENDU.md`.

---

## 8. Tests

```
mvn -f parc/pom.xml test            9 tests
mvn -f locations/pom.xml test      15 tests
cd frontend && npm test            15 tests
```

Les tests des services ne demandent ni réseau ni base externe.
`ServiceLocationsTest` n'utilise même pas Spring : le parc y est remplacé par un double de quelques lignes, et l'horloge par une horloge à pas fixe.

---

## 9. Ce que vous pouvez modifier

**Tout**, sauf le comportement fonctionnel.
Les 21 contrôles de `verifier-fonctionnel.mjs` doivent continuer à passer, et les tests fournis aussi.
Si vous jugez qu'une modification fonctionnelle est nécessaire, c'est peut-être vrai, mais il faut alors le justifier.

---

## 10. Problèmes courants

### `Cannot read properties of null (reading 'edgesOut')`
Ce message vient du résolveur de npm 10.9.3 : il tombe en résolvant l'ensemble des dépendances de pair de `vitest`.

Le projet livre un **`.npmrc`** qui active le mode de résolution *hérité* (`legacy-peer-deps=true`), et c'est dans ce mode que `package-lock.json` a été produit.
`npm ci` (comme `npm install`) fonctionnent donc **tels quels**, à condition de les lancer depuis `frontend/`, là où ce fichier se trouve.

Si vous supprimez `.npmrc`, `npm ci` refusera le verrou (« *can only install packages when your package.json and package-lock.json are in sync* »).

### Le premier `npm test` échoue en 60 s, sans exécuter un seul test

```
Error: [vitest-pool]: Failed to start forks worker for test files ...spec.ts
Caused by: Error: [vitest-pool-runner]: Timeout waiting for worker to respond
 Test Files  no tests     Duration  60.09s
```

Ce n'est pas vos tests qui échouent, mais c'est plutôt le **lanceur** (*runner*) qui n'arrive même pas à démarrer.
Lors du premier lancement, `ng test` build l'application (environ 20 secondes), puis Vitest tente d'ouvrir un processus par fichier de test.
Comme le dossier `node_modules` vient d'être écrit, votre antivirus est souvent en train de l'analyser, ce qui ralentit considérablement l'opération.
Vitest finit par abandonner au bout de 60 secondes ; il s'agit d'une limite fixe du paquet qu'aucune option de configuration ne permet de modifier.

**Relancez simplement `npm test`.**
La deuxième exécution profitera d'un cache chaud et se terminera en quelques secondes.
Si le problème persiste à chaque premier lancement, travaillez depuis un chemin de dossier plus court (voir ci-dessous).

### Le port 8080 ou 8081 est déjà occupé

`demarrer.mjs` refuse de démarrer plutôt que de laisser deux JVM se disputer le port.

Sous Windows :

```
netstat -ano | findstr :8080
taskkill /F /PID <le PID affiché>
```

### Les accents sont remplacés par des caractères bizarres
Les scripts de démarrage imposent déjà `-Dfile.encoding=UTF-8`.
Si vous lancez `java -jar` à la main sous Windows, ajoutez-le, ainsi que `-Dsun.stdout.encoding=UTF-8`.

### Sous Windows, travaillez depuis un chemin court

Extrayez l'archive dans un dossier comme `C:\tp3`.
Les chemins de `node_modules/` et de `target/` sont profonds, et la limite historique de 260 caractères de Windows est vite atteinte.
Les symptômes sont alors déroutants et sans rapport apparent avec la cause.

### Le navigateur n'affiche rien, ou « injoignable »
Les deux services dorsaux doivent écouter **avant** que la page ne charge.
L'interface distingue et affiche trois catégories d'échec : `injoignable` (personne n'a répondu), `refus-du-service` (le service a
répondu, et il a dit non) et `inattendu`.
La première se corrige en démarrant le service ; la deuxième se lit dans le message.

---

## 11. Arborescence

```
velos-tp3/
|-- parc/                 service du parc (port 8081)
|   `-- src/main/java/hai913i/tp3/parc/
|-- locations/            service des locations (port 8080)
|   `-- src/main/java/hai913i/tp3/locations/
|-- frontend/             interface web Angular (port 4200)
|   `-- src/app/
|-- scripts/
|   |-- demarrer.mjs      demarre un service, au premier plan
|   `-- lib.mjs           fonctions communes
|-- resources/            (resources.zip) scenarios, controles, sentinelle, agent
|-- examples/             (examples.zip)  micro-exemple Java, deux lecteurs
|-- package.json          trois raccourcis : npm run parc, npm run locations, npm run frontend
`-- .gitignore
```

`resources/` et `examples/` viennent des deux autres archives (voir §0) ;
`starter.zip` ne les contient pas.
