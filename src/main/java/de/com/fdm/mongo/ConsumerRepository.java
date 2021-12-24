package de.com.fdm.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConsumerRepository extends MongoRepository<Consumer, String> {
    Consumer findByCallback(String callback);
    List<Consumer> findByChannels(String channel);
}
