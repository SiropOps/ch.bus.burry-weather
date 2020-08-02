package ch.bus.weather.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.bus.weather.client.GpsClient;
import ch.bus.weather.dto.OutsideDTO;
import ch.bus.weather.entity.Outside;
import ch.bus.weather.repository.OutsideRepository;

@Service
public class WeatherService {

  private static Logger log = LoggerFactory.getLogger(WeatherService.class);
  private static boolean RUNNING = true;

  @Autowired
  private OutsideRepository outsideRepository;

  @Autowired
  private GpsClient gpsClient;

  private boolean isNull(Double value) {
    return (value == null ? true : Double.isNaN(value));
  }

  @Transactional
  @RabbitListener(queues = "smart-sensor")
  public void receiveMessage(final OutsideDTO outsideMessage) {
    if (outsideMessage == null || !RUNNING)
      return;

    if (isNull(outsideMessage.getTemperature()) || isNull(outsideMessage.getHumidity()))
      return;

    log.info("Received message as specific class: {}", outsideMessage.toString());

    Outside outside = new Outside();
    BeanUtils.copyProperties(outsideMessage, outside);
    outside.setTime(this.gpsClient.getSpeakingClock().getDate());
    this.outsideRepository.save(outside);
  }

}
