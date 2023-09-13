package ch.bus.weather.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    List<OutsideDTO> list = this.outsideRepository.findAll().stream().sorted()
        .map(CopyBean::outside).collect(Collectors.toList());

    final List<OutsideDTO> listFinal = new ArrayList<>();

    LocalDateTime startDateTime = list.stream().map(d -> d.getTime()).min(Date::compareTo)
        .orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime endDateTime = list.stream().map(d -> d.getTime()).max(Date::compareTo)
        .orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    Stream.iterate(startDateTime, d -> d.plusMinutes(15))
        .limit((ChronoUnit.MINUTES.between(startDateTime, endDateTime) / 15) + 1).forEach(time -> {
          List<OutsideDTO> subList = list.stream().filter(d -> {
            return d.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .isBefore(time);
          }).collect(Collectors.toList());
          list.removeAll(subList);

          double averageTemp = subList.stream().map(d -> d.getTemperature())
              .mapToDouble(Double::doubleValue).average().orElse(Double.NEGATIVE_INFINITY);

          if (Double.NEGATIVE_INFINITY != averageTemp) {
            OutsideDTO dto = new OutsideDTO();
            dto.setTemperature(averageTemp);
            dto.setTime(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
            listFinal.add(dto);
          }
        });

    return listFinal;
  }

  public List<InsideDTO> getAllInsides() {

    List<InsideDTO> list = this.insideRepository.findAll().stream().map((i) -> {
      i.setTime(Date.from(this.convertToLocalDateViaInstant(i.getTime())
          .truncatedTo(ChronoUnit.MINUTES).atZone(ZoneId.systemDefault()).toInstant()));
      return i;
    }).collect(Collectors.toMap(Inside::getTime, v -> v, (v1, v2) -> v1)).values().stream().sorted()
        .map(CopyBean::inside).collect(Collectors.toList());


    final List<InsideDTO> listFinal = new ArrayList<>();

    LocalDateTime startDateTime = list.stream().map(d -> d.getTime()).min(Date::compareTo)
        .orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime endDateTime = list.stream().map(d -> d.getTime()).max(Date::compareTo)
        .orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    Stream.iterate(startDateTime, d -> d.plusMinutes(15))
        .limit((ChronoUnit.MINUTES.between(startDateTime, endDateTime) / 15) + 1).forEach(time -> {
          List<InsideDTO> subList = list.stream().filter(d -> {
            return d.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .isBefore(time);
          }).collect(Collectors.toList());
          list.removeAll(subList);

          double averageTemp = subList.stream().map(d -> d.getTemperature())
              .mapToDouble(Double::doubleValue).average().orElse(Double.NEGATIVE_INFINITY);

          if (Double.NEGATIVE_INFINITY != averageTemp) {
            InsideDTO dto = new InsideDTO();
            dto.setTemperature(averageTemp);
            dto.setTime(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
            listFinal.add(dto);
          }
        });

    return listFinal;

  }

  public LocalDateTime convertToLocalDateViaInstant(Date dateToConvert) {
    return LocalDateTime.ofInstant(
        Optional.ofNullable(dateToConvert).orElse(new Date()).toInstant(), ZoneId.systemDefault());
  }

  public List<MonlesiDTO> getAllMonlesis() {

    List<MonlesiDTO> list = this.monlesiRepository.findAll().stream().sorted()
        .map(CopyBean::monlesi).collect(Collectors.toList());

    final List<MonlesiDTO> listFinal = new ArrayList<>();

    LocalDateTime startDateTime = list.stream().map(d -> d.getTime()).min(Date::compareTo)
        .orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime endDateTime = list.stream().map(d -> d.getTime()).max(Date::compareTo)
        .orElse(new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    Stream.iterate(startDateTime, d -> d.plusMinutes(15))
        .limit((ChronoUnit.MINUTES.between(startDateTime, endDateTime) / 15) + 1).forEach(time -> {
          List<MonlesiDTO> subList = list.stream().filter(d -> {
            return d.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                .isBefore(time);
          }).collect(Collectors.toList());
          list.removeAll(subList);

          double averageTemp = subList.stream().map(d -> d.getTemperature())
              .mapToDouble(Double::doubleValue).average().orElse(Double.NEGATIVE_INFINITY);

          if (Double.NEGATIVE_INFINITY != averageTemp) {
            MonlesiDTO dto = new MonlesiDTO();
            dto.setTemperature(averageTemp);
            dto.setTime(Date.from(time.atZone(ZoneId.systemDefault()).toInstant()));
            listFinal.add(dto);
          }
        });

    return listFinal;
  }

}
