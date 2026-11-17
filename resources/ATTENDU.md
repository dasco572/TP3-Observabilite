# TP 3 — Valeurs fonctionnelles attendues
Ce fichier décrit ce que l'application **CIBLE** produit, fonctionnellement, sur un parc fraîchement démarré.  
Il ne contient **AUCUNE** réponse d'observabilité.

---

## 1. Le parc de départ

6 stations, 25 vélos, dont 21 retirables.

| Code         | Nom                 | Bornes | Retirables | Maintenance | État             |
| :----------- | :------------------ | :----: | :--------: | :---------: | :--------------- |
| `ST-ARCEAUX` | Les Arceaux         |   12   |     4      |      1      | Ouverte          |
| `ST-COMEDIE` | Place de la Comédie |   20   |     7      |      0      | Ouverte          |
| `ST-GARE`    | Gare Saint-Roch     |   16   |     0      |      2      | Aucun vélo       |
| `ST-MOSSON`  | Mosson              |   10   |     3      |      0      | Ouverte          |
| `ST-PEYROU`  | Promenade du Peyrou |   8    |     5      |      0      | **HORS SERVICE** |
| `ST-TRIOLET` | Campus Triolet      |   14   |     2      |      1      | Ouverte          |

Les vélos sont numérotés `V-0001` à `V-0025`, attribués station par station dans l'ordre des codes, les vélos disponibles avant ceux en maintenance.

Une station est **LOUABLE** quand elle n'est pas hors service et qu'il lui reste au moins un vélo retirable : `ST-ARCEAUX`, `ST-COMEDIE`, `ST-MOSSON`, `ST-TRIOLET`.

---

## 2. Le cycle d'une location

- `POST /locations` `{ codeStation: "ST-COMEDIE", courriel, motDePasse }`  
  ➔ **201** : référence `L-0001`, vélo `V-0006`, état `EN_COURS`  
  *Le vélo retiré est toujours le **PREMIER** disponible de la station, dans l'ordre des identifiants. Les références sont attribuées dans l'ordre d'ouverture : `L-0001`, `L-0002`, ...*

- `GET /locations/L-0001/rapport`  
  ➔ **200** : `indiceRotation` 100, `locationsDepuis` 1  
  *L'indice de rotation est la part des vélos rattachés à la station qui sont retirables, en pour cent. Sur le parc de départ :*  
  * `ST-ARCEAUX` : 80
  * `ST-COMEDIE` : 100
  * `ST-GARE` : 0
  * `ST-MOSSON` : 100
  * `ST-PEYROU` : 100
  * `ST-TRIOLET` : 67

- `POST /locations/L-0001/restitution` `{ codeStation: "ST-ARCEAUX" }`  
  ➔ **200** : `dureeMinutes` 1, `montant` 1,05, état `CLOTUREE`  
  *Le tarif : 1,00 € de prise en charge, plus 0,05 € par minute **ENTAMÉE**, et toute location est facturée au moins une minute. Une location ouverte et rendue dans la même minute coûte donc 1,05 €. Dix minutes et une seconde en coûtent 1,55 € (11 minutes facturées).*

---

## 3. Les six refus

| Action                              |  Code   | Motif                            |
| :---------------------------------- | :-----: | :------------------------------- |
| Ouvrir à `ST-GARE`                  | **409** | Aucun vélo disponible            |
| Ouvrir à `ST-PEYROU`                | **503** | Station hors service             |
| Ouvrir à `ST-AILLEURS`              | **404** | Station inconnue du parc         |
| Ouvrir avec un mauvais mot de passe | **401** | Abonné ou mot de passe incorrect |
| Restituer une référence inconnue    | **404** | Location introuvable             |
| Restituer deux fois                 | **409** | Location déjà clôturée           |

Les corps d'erreur suivent le format `ProblemDetail` de la RFC 9457 : un objet JSON portant `status`, `title` et `detail`.

Noter la correspondance choisie : un **REFUS** du parc reste un conflit (**409**), tandis qu'une **INDISPONIBILITÉ** du parc devient une indisponibilité du service des locations (**503**). Un appelant qui ne verrait qu'un code 500 ne saurait pas lequel des deux services est en cause.

---

## 4. Les durées

Sur une machine de bureau ordinaire, six des sept scénarios répondent en quelques dizaines de millisecondes.
Le sixième (le rapport) demande environ un demi-millier de millisecondes, et cette durée est **FIXE** ; elle ne dépend ni de la machine, ni du nombre de locations.