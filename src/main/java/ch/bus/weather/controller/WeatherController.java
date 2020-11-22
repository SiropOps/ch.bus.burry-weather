package ch.bus.weather.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ch.bus.weather.dto.InsideDTO;
import ch.bus.weather.dto.OutsideDTO;
import ch.bus.weather.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

  @Autowired
  private WeatherService weatherService;

  @GetMapping("/outside")
  public ResponseEntity<OutsideDTO> getOutside() {
    return new ResponseEntity<>(this.weatherService.getLastOutside(), HttpStatus.OK);
  }

  @GetMapping("/outsides")
  public ResponseEntity<List<OutsideDTO>> getOutsides() {
    return new ResponseEntity<>(this.weatherService.getAllOutsides(), HttpStatus.OK);
  }

  @GetMapping("/inside")
  public ResponseEntity<InsideDTO> getInside() {
    return new ResponseEntity<>(this.weatherService.getLastInside(), HttpStatus.OK);
  }

  @GetMapping("/insides")
  public ResponseEntity<List<InsideDTO>> getInsides() {
    return new ResponseEntity<>(this.weatherService.getAllInsides(), HttpStatus.OK);
  }
}
