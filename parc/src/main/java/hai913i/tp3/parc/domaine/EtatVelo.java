package hai913i.tp3.parc.domaine;

/** État d'un vélo dans le parc. */
public enum EtatVelo {

    /** Attaché à une borne, prêt à être retiré. */
    DISPONIBLE,

    /** Retiré d'une station, en cours d'utilisation. */
    EN_COURSE,

    /** Immobilisé pour réparation et ne compte pas dans la disponibilité. */
    MAINTENANCE
}