package storehouse.backoffice.spots.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class Configurator {
	@Bean
	ObjectMapper getMapperBean() {
		return new ObjectMapper();
	}
}
