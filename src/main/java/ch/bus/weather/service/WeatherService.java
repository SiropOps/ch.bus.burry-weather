package ch.bus.weather.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.bus.weather.client.GpsClient;
import ch.bus.weather.dto.InsideDTO;
import ch.bus.weather.dto.MonlesiDTO;
import ch.bus.weather.dto.OutsideDTO;
import ch.bus.weather.entity.Inside;
import ch.bus.weather.entity.Monlesi;
import ch.bus.weather.entity.Outside;
import ch.bus.weather.repository.InsideRepository;
import ch.bus.weather.repository.MonlesiRepository;
import ch.bus.weather.repository.OutsideRepository;
import ch.bus.weather.utils.CopyBean;

@Service
public class WeatherService {

  private static Logger log = LoggerFactory.getLogger(WeatherService.class);
  private static boolean RUNNING = true;

  @Autowired
  private OutsideRepository outsideRepository;

  @Autowired
  private InsideRepository insideRepository;

  @Autowired
  private MonlesiRepository monlesiRepository;

  @Autowired
  private GpsClient gpsClient;

  private OutsideDTO lastOutside = null;
  private InsideDTO lastInside = null;
  private MonlesiDTO lastMonlesi = null;

  private boolean isNull(Double value) {
    return (value == null ? true : Double.isNaN(value));
  }

  @Transactional
  @RabbitListener(queues = "smart-sensor-outside")
  public void receiveMessage(final OutsideDTO outsideMessage) {
    if (outsideMessage == null || !RUNNING)
      return;

    if (isNull(outsideMessage.getTemperature()) || isNull(outsideMessage.getHumidity()))
      return;

    if (outsideMessage.getTemperature() > 80 || outsideMessage.getTemperature() < -40)
      return;

    if (Optional.ofNullable(getLastOutside()).isPresent()
        && Optional.ofNullable(getLastOutside().getTemperature()).isPresent()) {
      if (getLastOutside().getTemperature() - 10 > outsideMessage.getTemperature()
          || getLastOutside().getTemperature() + 10 < outsideMessage.getTemperature()) {
        return;
      }
    }

    log.info("Received message as specific class: {}", outsideMessage.toString());

    outsideMessage.setTime(this.gpsClient.getSpeakingClock().getDate());

    Outside outside = new Outside();
    BeanUtils.copyProperties(outsideMessage, outside);

    this.outsideRepository.save(outside);

    lastOutside = outsideMessage;
  }

  public OutsideDTO getLastOutside() {
    return lastOutside;
  }


  @Transactional
  @RabbitListener(queues = "smart-sensor-monlesi")
  public void receiveMessage(final MonlesiDTO monlesiMessage) {
    if (monlesiMessage == null || !RUNNING)
      return;

    if (isNull(monlesiMessage.getTemperature()) || isNull(monlesiMessage.getHumidity()))
      return;

    if (monlesiMessage.getTemperature() > 80 || monlesiMessage.getTemperature() < -40)
      return;

    if (Optional.ofNullable(getLastMonlesi()).isPresent()
        && Optional.ofNullable(getLastMonlesi().getTemperature()).isPresent()) {
      if (getLastMonlesi().getTemperature() - 10 > monlesiMessage.getTemperature()
          || getLastMonlesi().getTemperature() + 10 < monlesiMessage.getTemperature()) {
        return;
      }
    }

    log.info("Received message as specific class: {}", monlesiMessage.toString());

    monlesiMessage.setTime(this.gpsClient.getSpeakingClock().getDate());

    Monlesi monlesi = new Monlesi();
    BeanUtils.copyProperties(monlesiMessage, monlesi);

    this.monlesiRepository.save(monlesi);

    lastMonlesi = monlesiMessage;
  }

  public MonlesiDTO getLastMonlesi() {
    return lastMonlesi;
  }


  @Transactional
  @RabbitListener(queues = "dht-22")
  public void receiveMessage(final InsideDTO insideMessage) {
    if (insideMessage == null || !RUNNING)
      return;

    if (isNull(insideMessage.getTemperature()) || isNull(insideMessage.getHumidity()))
      return;

    if (insideMessage.getTemperature() > 80 || insideMessage.getTemperature() < -40)
      return;


    if (Optional.ofNullable(getLastInside()).isPresent()
        && Optional.ofNullable(getLastInside().getTemperature()).isPresent()) {
      if (getLastInside().getTemperature() - 5 > insideMessage.getTemperature()
          || getLastInside().getTemperature() + 5 < insideMessage.getTemperature()) {
        return;
      }
    }

    log.info("Received message as specific class: {}", insideMessage.toString());

    if (!isNull(Optional.ofNullable(lastInside).orElse(new InsideDTO()).getTemperature())
        && lastInside.getTemperature().equals(insideMessage.getTemperature()))
      return;

    insideMessage.setTime(this.gpsClient.getSpeakingClock().getDate());

    Inside inside = new Inside();
    BeanUtils.copyProperties(insideMessage, inside);

    this.insideRepository.save(inside);

    lastInside = insideMessage;
  }

  public InsideDTO getLastInside() {
    return lastInside;
  }

  public List<OutsideDTO> getAllOutsides() {
    return this.outsideRepository.findAll().stream().sorted().map(CopyBean::outside)
        .collect(Collectors.toList());
  }

  public List<InsideDTO> getAllInsides() {
    return this.insideRepository.findAll().stream().sorted().map(CopyBean::inside)
        .collect(Collectors.toList());
  }

  public List<MonlesiDTO> getAllMonlesis() {
    return this.monlesiRepository.findAll().stream().sorted().map(CopyBean::monlesi)
        .collect(Collectors.toList());
  }

}
