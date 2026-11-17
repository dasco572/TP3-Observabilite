package hai913i.tp3.locations.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * L'horloge du service.
 *
 * <p>Le métier ne lit jamais l'heure directement.
 * Il reçoit plutôt une horloge.
 * Cela permet d'écrire un test de facturation sans attendre réellement dix minutes.</p>
 */
@Configuration
public class ConfigurationHorloge {

    @Bean
    Clock horloge() {
        return Clock.systemUTC();
    }
}