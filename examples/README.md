# TP 3 — Exemples exécutables : instrumentation et diagnostic
Trois outils minuscules sur le domaine d'un convertisseur d'unités de distance.  
Ces exemples montrent des mécanismes ; ils ne disent rien de ce qu'il faut instrumenter dans l'application cible, ni où, ni pourquoi.

**Prérequis :** JDK 17, Maven, Node.js.

**Où extraire, et depuis où lancer.** Cette archive porte la racine
`velos-tp3/` : extrayez-la **dans le même dossier parent** que `starter.zip`
et `resources.zip`, et son contenu se place de lui-même en
`velos-tp3/examples/`. **Toutes** les commandes ci-dessous se lancent depuis
la racine `velos-tp3/`.

---

## 1. Demonstration.java — les quatre mécanismes

```bash
mvn -f examples/pom.xml clean package

java -jar examples/target/exemples.jar

java -javaagent:opentelemetry-javaagent.jar -jar examples/target/exemples.jar

```

Le JAR est le même dans les deux cas.
C'est tout l'argument de l'instrumentation zéro-code.

Ce qu'il faut comparer entre les deux sorties :

* Sans agent, `trace_id=` et `span_id=` sont **VIDES** ;
* Avec agent, ils sont remplis, mais **SEULEMENT** sur les lignes écrites à l'intérieur d'un span actif. La première ligne de la démonstration est journalisée hors de tout span et reste sans identifiant, et n'est donc corrélable à rien. Ce n'est pas un défaut, c'est la règle.

Les quatre mécanismes, dans l'ordre du fichier :

1. **JOURNALISER**
Appel paramétré, journal structuré, choix du niveau, et ce qu'on ne journalise pas.
2. **SPAN EXPLICITE**
Un span écrit à la main, avec des attributs du métier et un événement daté. Et `makeCurrent()`, sans quoi rien ne s'y rattache.
3. **ANNOTATION**
`@WithSpan` : la forme la plus légère. C'est **L'AGENT** qui crée le span, pas le code. Sans agent, l'annotation ne fait rien. Noter, dans la sortie, que le *tracer* de ce span n'est pas celui des spans écrits à la main. `@SpanAttribute` fait passer un paramètre en attribut.
4. **ÉCHEC**
`setStatus(ERROR)` + `recordException` sur le span, **ET** une ligne de journal avec l'exception en **DERNIER** argument. Le `catch` est à **L'INTÉRIEUR** de la portée, parce qu'en Java le `catch` d'un *try-with-resources* s'exécute **APRÈS** la fermeture de la ressource, et une ligne d'erreur écrite dehors sortirait sans `trace_id`.

Pour récupérer l'agent :

```bash
node resources/telecharger-agent.mjs

```

---

## 2. lire-trace.mjs — où est passé le temps

```bash
node examples/lire-trace.mjs --service <nom>
node examples/lire-trace.mjs --trace <identifiant>
node examples/lire-trace.mjs --service <nom> --toutes

```

Imprime une trace en arbre : un span par ligne, sa durée, sa part du total, et le service qui l'a produit.
En bas, les services traversés et les *tracers*.

**Options :** `--jaeger <url>` (défaut : `http://localhost:16686`).

---

## 3. relier-journal-trace.mjs — ce que le programme a dit

```bash
node examples/relier-journal-trace.mjs --journal <fichier>
node examples/relier-journal-trace.mjs --journal <fichier> --trace <id>

```

Sans `--trace`, liste les identifiants de trace vus dans le fichier, avec le nombre de lignes qui les portent.
Avec `--trace`, n'imprime que les lignes de cette trace.
Il reconnaît les deux formes : `trace_id=abcd...` (console) et `"trace_id":"abcd..."` (journal structuré).

C'est l'autre moitié de `lire-trace.mjs` :

* `lire-trace.mjs` montre **où est passé le temps** ;
* `relier-journal-trace.mjs` montre **ce que le programme avait à dire** pendant ce temps-là.

Les deux ensemble forment la corrélation **journal ↔ trace**.