package ch.bus.weather.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.bus.weather.entity.Outside;

public interface OutsideRepository extends MongoRepository<Outside, Long> {

}
