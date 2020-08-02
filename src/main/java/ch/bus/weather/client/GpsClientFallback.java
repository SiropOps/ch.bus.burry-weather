package ch.bus.weather.client;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.bus.weather.dto.SpeakingClockDTO;

public class GpsClientFallback implements GpsClient {

  protected final Throwable cause;

  private static Logger log = LoggerFactory.getLogger(GpsClientFallback.class);


  public GpsClientFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public SpeakingClockDTO getSpeakingClock() {
    log.error(cause.getMessage(), cause);
    return new SpeakingClockDTO(new Date());
  }


}
