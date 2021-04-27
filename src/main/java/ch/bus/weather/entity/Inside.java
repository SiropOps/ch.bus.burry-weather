package ch.bus.weather.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "weather_inside")
public class Inside implements Serializable, Comparable<Inside> {

  private static final long serialVersionUID = 1L;

  private long id;
  private Double temperature;
  private Double humidity;

  private Date time; // Time


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_weather_inside")
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @Column(name = "temperature")
  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  @Column(name = "humidity")
  public Double getHumidity() {
    return humidity;
  }

  public void setHumidity(Double humidity) {
    this.humidity = humidity;
  }

  @Column(name = "time")
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
    return Long.compare(this.id, o.id);
  }



}
