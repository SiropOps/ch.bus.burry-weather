package ch.bus.weather.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ch.bus.weather.dto.SpeakingClockDTO;

@FeignClient(name = "gps", url = "http://192.168.8.210:8011/api/gps",
    fallbackFactory = GpsClientFallbackFactory.class)
public interface GpsClient {

  @RequestMapping(method = RequestMethod.GET, value = "/speaking_clock",
      consumes = "application/json")
  SpeakingClockDTO getSpeakingClock();

}
