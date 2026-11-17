/**
 * Ce que l'application manipule, côté navigateur.
 *
 * <p>Des types TypeScript (non des classes) :
 * rien ici n'a de comportement.</p>
 *
 * <p>On distingue deux familles :
 * ce que l'application <b>reçoit</b> et ce qu'elle <b>envoie</b>.</p>
 */

export interface Station {
  code: string;
  nom: string;
  capacite: number;
  velosDisponibles: number;
  horsService: boolean;
  /** Vrai quand un retrait peut aboutir (décidé par le service) */
  louable: boolean;
}

export type EtatLocation = 'EN_COURS' | 'CLOTUREE';

export interface Location {
  reference: string;
  identifiantVelo: string;
  codeDepart: string;
  codeArrivee: string | null;
  dureeMinutes: number;
  montant: number | null;
  etat: EtatLocation;
}

export interface Rapport {
  reference: string;
  codeDepart: string;
  indiceRotation: number;
  locationsDepuis: number;
}

/**
 * La charge utile envoyée pour ouvrir une location.
 *
 * <p>Elle porte une adresse électronique complète et un mot de passe,
 * parce que le service doit reconnaître l'abonné.
 * Ce type n'est utilisé que par la couche d'accès au service.</p>
 */
export interface DemandeLocation {
  codeStation: string;
  courriel: string;
  motDePasse: string;
}

export interface DemandeRestitution {
  codeStation: string;
}