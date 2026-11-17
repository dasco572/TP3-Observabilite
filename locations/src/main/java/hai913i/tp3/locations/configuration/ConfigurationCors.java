package hai913i.tp3.locations.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * L'interface web est servie sur une autre origine que ce service,
 * et le navigateur donc exige une autorisation explicite.
 *
 * <p>La liste des en-têtes autorisés ne doit contenir que les origines
 * dont l'application fonctionnelle a besoin.</p>
 */
@Configuration
public class ConfigurationCors implements WebMvcConfigurer {

    private final String origineFrontale;

    ConfigurationCors(@Value("${locations.origine-frontale}") String origineFrontale) {
        this.origineFrontale = origineFrontale;
    }

    @Override
    public void addCorsMappings(CorsRegistry registre) {
        registre.addMapping("/**")
                .allowedOrigins(origineFrontale)
                .allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type");
    }
}