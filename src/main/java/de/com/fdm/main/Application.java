package de.com.fdm.main;

import de.com.fdm.mongo.Consumer;
import de.com.fdm.mongo.ConsumerRepository;
import de.com.fdm.tmi.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

@SpringBootApplication(scanBasePackages = {"de.com.fdm.service", "de.com.fdm.tmi"})
@EnableMongoRepositories(basePackageClasses = ConsumerRepository.class)
public class Application {

    @Autowired
    private Reader reader;

    @Autowired
    private ConsumerRepository consumerRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void readInitialChannels() {
        List<Consumer> consumers = this.consumerRepository.findAll();

        for (Consumer consumer : consumers) {
            this.reader.joinChannels(consumer.getChannels());
        }
    }
}
