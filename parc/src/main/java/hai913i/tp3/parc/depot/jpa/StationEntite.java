package hai913i.tp3.parc.depot.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * La station, telle qu'elle est stockée.
 *
 * <p>Distincte de {@link hai913i.tp3.parc.domaine.Station} :
 * l'annotation de persistance ne traverse pas la frontière du métier.</p>
 */
@Entity
@Table(name = "station")
public class StationEntite {

    @Id
    @Column(length = 32)
    private String code;

    @Column(nullable = false, length = 80)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    @Column(name = "hors_service", nullable = false)
    private boolean horsService;

    protected StationEntite() {
        //  requis par JPA
    }

    StationEntite(String code, String nom, int capacite, boolean horsService) {
        this.code = code;
        this.nom = nom;
        this.capacite = capacite;
        this.horsService = horsService;
    }

    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public boolean isHorsService() {
        return horsService;
    }
}
