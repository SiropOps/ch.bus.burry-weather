package ch.bus.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ch.bus.weather.entity.Inside;

public interface InsideRepository extends JpaRepository<Inside, Long> {

}
