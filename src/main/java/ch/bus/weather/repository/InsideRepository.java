package ch.bus.weather.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.bus.weather.entity.Inside;

public interface InsideRepository extends MongoRepository<Inside, Long> {

}
