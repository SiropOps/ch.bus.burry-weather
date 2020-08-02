package ch.bus.weather.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "weather_outside")
public class Outside implements Serializable {

  private static final long serialVersionUID = 1L;

  private long id;
  private Double temperature;
  private Double humidity;
  private Double battery;

  private Date time; // Time


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_weather_outside")
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

  @Column(name = "battery")
  public Double getBattery() {
    return battery;
  }

  public void setBattery(Double battery) {
    this.battery = battery;
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
    return "Outside [id=" + id + ", temperature=" + temperature + ", humidity=" + humidity
        + ", battery=" + battery + ", time=" + time + "]";
  }



}
