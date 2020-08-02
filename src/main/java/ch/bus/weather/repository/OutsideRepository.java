package ch.bus.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ch.bus.weather.entity.Outside;

public interface OutsideRepository extends JpaRepository<Outside, Long>, OutsideRepositoryCustom {

}
