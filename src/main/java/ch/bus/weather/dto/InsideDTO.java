package ch.bus.weather.dto;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InsideDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  private Double temperature;
  private Double humidity;
  private Date time;

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Double getHumidity() {
    return humidity;
  }

  public void setHumidity(Double humidity) {
    this.humidity = humidity;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  @Override
  public String toString() {
    return "OutsideDTO [temperature=" + temperature + ", humidity=" + humidity + ", time=" + time
        + "]";
  }

}
