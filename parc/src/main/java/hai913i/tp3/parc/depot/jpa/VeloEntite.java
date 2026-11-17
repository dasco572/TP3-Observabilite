package hai913i.tp3.parc.depot.jpa;

import hai913i.tp3.parc.domaine.EtatVelo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Le vélo, tel qu'il est stocké.
 * 
 * <p>Distinct de {@link hai913i.tp3.parc.domaine.Velo} :
 * l'annotation de persistance ne traverse pas la frontière du métier.</p>
*/
@Entity
@Table(name = "velo")
public class VeloEntite {

    @Id
    @Column(length = 32)
    private String identifiant;

    @Column(name = "code_station", length = 32)
    private String codeStation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EtatVelo etat;

    protected VeloEntite() {
        //  requis par JPA
    }

    VeloEntite(String identifiant, String codeStation, EtatVelo etat) {
        this.identifiant = identifiant;
        this.codeStation = codeStation;
        this.etat = etat;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public String getCodeStation() {
        return codeStation;
    }

    public EtatVelo getEtat() {
        return etat;
    }

    void placer(String codeStation, EtatVelo etat) {
        this.codeStation = codeStation;
        this.etat = etat;
    }
}
