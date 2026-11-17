package hai913i.tp3.locations.depot.jpa;

import java.math.BigDecimal;
import java.time.Instant;

import hai913i.tp3.locations.domaine.EtatLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** La location, telle qu'elle est stockée. */
@Entity
@Table(name = "location")
public class LocationEntite {

    @Id
    @Column(length = 32)
    private String reference;

    @Column(name = "identifiant_velo", nullable = false, length = 32)
    private String identifiantVelo;

    @Column(name = "code_depart", nullable = false, length = 32)
    private String codeDepart;

    @Column(name = "code_arrivee", length = 32)
    private String codeArrivee;

    @Column(nullable = false)
    private Instant debut;

    private Instant fin;

    @Column(name = "duree_minutes", nullable = false)
    private int dureeMinutes;

    @Column(precision = 8, scale = 2)
    private BigDecimal montant;

    /**
     * Le domaine de l'adresse de l'abonné.
     *
     * <p>Il n'existe aucune colonne pour l'adresse complète, et aucune pour
     * le mot de passe.</p>
     */
    @Column(name = "domaine_abonne", nullable = false, length = 80)
    private String domaineAbonne;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EtatLocation etat;

    protected LocationEntite() {
        //  requis par JPA
    }

    LocationEntite(String reference, String identifiantVelo, String codeDepart,
                   Instant debut, String domaineAbonne) {
        this.reference = reference;
        this.identifiantVelo = identifiantVelo;
        this.codeDepart = codeDepart;
        this.debut = debut;
        this.domaineAbonne = domaineAbonne;
        this.dureeMinutes = 0;
        this.etat = EtatLocation.EN_COURS;
    }

    public String getReference() {
        return reference;
    }

    public String getIdentifiantVelo() {
        return identifiantVelo;
    }

    public String getCodeDepart() {
        return codeDepart;
    }

    public String getCodeArrivee() {
        return codeArrivee;
    }

    public Instant getDebut() {
        return debut;
    }

    public Instant getFin() {
        return fin;
    }

    public int getDureeMinutes() {
        return dureeMinutes;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public String getDomaineAbonne() {
        return domaineAbonne;
    }

    public EtatLocation getEtat() {
        return etat;
    }

    void cloturer(String codeArrivee, Instant fin, int dureeMinutes, BigDecimal montant) {
        this.codeArrivee = codeArrivee;
        this.fin = fin;
        this.dureeMinutes = dureeMinutes;
        this.montant = montant;
        this.etat = EtatLocation.CLOTUREE;
    }
}
