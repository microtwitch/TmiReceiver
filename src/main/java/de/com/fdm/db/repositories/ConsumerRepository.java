package de.com.fdm.db.repositories;

import de.com.fdm.db.data.Consumer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ConsumerRepository extends CrudRepository<Consumer, Long> {
    Consumer findByCallback(String callback);
}
