package ch.bus.weather.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.bus.weather.entity.Monlesi;

public interface MonlesiRepository extends MongoRepository<Monlesi, Long> {

}
