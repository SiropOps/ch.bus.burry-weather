package ch.bus.weather;

import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@EnableAsync
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
public class BurryWeather {

  public static void main(String[] args) {
    SpringApplication.run(BurryWeather.class, args);
  }

  @Bean
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("BurryWeatherLookup-");
    executor.initialize();
    return executor;
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToEnable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
    builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
    return builder.build();
  }

}
