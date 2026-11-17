package hai913i.tp3.locations.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Le client HTTP qui parle au service du parc.
 *
 * <p>Le transport est fixé explicitement sur le client HTTP du JDK.</p>
 */
@Configuration
public class ConfigurationHttp {

    @Bean
    RestClient clientParc(@Value("${locations.url-parc}") String urlParc) {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory())
                .baseUrl(urlParc)
                .build();
    }
}