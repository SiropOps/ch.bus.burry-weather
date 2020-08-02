package ch.bus.weather.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class OutsideRepositoryImpl implements OutsideRepositoryCustom {

  @PersistenceContext
  EntityManager entityManager;

}
