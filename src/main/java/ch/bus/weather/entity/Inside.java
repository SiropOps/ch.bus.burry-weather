package ch.bus.weather.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.annotation.Id;


public class Inside implements Serializable, Comparable<Inside> {

  private static final long serialVersionUID = 1L;

  private String id;
  private Double temperature;
  private Double humidity;

  private Date time; // Time

  @Id
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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
    return "inside [id=" + id + ", temperature=" + temperature + ", humidity=" + humidity
        + ", time=" + time + "]";
  }

  @Override
  public int compareTo(Inside o) {
    if (!Optional.ofNullable(o).isPresent())
      return 1;
    if (Optional.ofNullable(this.time).isPresent() && Optional.ofNullable(o.time).isPresent())
      return this.time.compareTo(o.time);
    return (Optional.ofNullable(this.id).orElse("").compareTo(o.id));
  }



}
