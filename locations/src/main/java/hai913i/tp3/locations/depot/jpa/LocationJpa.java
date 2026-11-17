package hai913i.tp3.locations.depot.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Accès Spring Data aux locations. */
public interface LocationJpa extends JpaRepository<LocationEntite, String> {

    List<LocationEntite> findAllByOrderByReferenceDesc();

    int countByCodeDepart(String codeDepart);
}
