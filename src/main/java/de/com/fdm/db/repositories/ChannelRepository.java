package de.com.fdm.db.repositories;

import de.com.fdm.db.data.Channel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChannelRepository extends CrudRepository<Channel, Long> {
    Channel findByName(String name);
}
