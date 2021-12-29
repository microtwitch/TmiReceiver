package de.com.fdm.db.services;

import de.com.fdm.db.data.Consumer;
import de.com.fdm.db.repositories.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepository consumerRepository;

    public void saveConsumer(Consumer consumer) {
        this.consumerRepository.save(consumer);
    }
}
