package hai913i.tp3.parc.depot.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Accès Spring Data aux stations. */
public interface StationJpa extends JpaRepository<StationEntite, String> {

    List<StationEntite> findAllByOrderByCodeAsc();
}
