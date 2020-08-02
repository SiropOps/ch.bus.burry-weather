package ch.bus.weather.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EntityScan(value = {"ch.bus.weather"})
public class HibernateConfig {

}
