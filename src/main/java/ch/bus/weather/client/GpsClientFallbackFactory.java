package ch.bus.weather.client;

import org.springframework.stereotype.Component;
import feign.hystrix.FallbackFactory;

@Component
public class GpsClientFallbackFactory implements FallbackFactory<GpsClient> {

  @Override
  public GpsClient create(Throwable cause) {
    return new GpsClientFallback(cause);
  }


}
