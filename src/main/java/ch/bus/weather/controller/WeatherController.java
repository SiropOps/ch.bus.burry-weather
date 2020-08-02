package ch.bus.weather.controller;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ch.bus.weather.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

  @Autowired
  private WeatherService weatherService;

  @GetMapping("/test/{value}")
  public ResponseEntity<String> getTest(@PathVariable("value") String value) {
    return new ResponseEntity<>(value + ":" + new Date().toString(), HttpStatus.OK);
  }

}
