package ch.bus.weather.utils;

import java.util.Optional;
import org.springframework.beans.BeanUtils;
import ch.bus.weather.dto.InsideDTO;
import ch.bus.weather.dto.MonlesiDTO;
import ch.bus.weather.dto.OutsideDTO;
import ch.bus.weather.entity.Inside;
import ch.bus.weather.entity.Monlesi;
import ch.bus.weather.entity.Outside;

public class CopyBean {

  public static OutsideDTO outside(Outside item) {
    return copy(item, new OutsideDTO());
  }

  public static InsideDTO inside(Inside item) {
    return copy(item, new InsideDTO());
  }

  public static MonlesiDTO monlesi(Monlesi item) {
    return copy(item, new MonlesiDTO());
  }

  private static <T extends Object> T copy(Object item, T dto) {
    if (!Optional.ofNullable(item).isPresent())
      return dto;
    BeanUtils.copyProperties(item, dto);
    return dto;
  }

}
