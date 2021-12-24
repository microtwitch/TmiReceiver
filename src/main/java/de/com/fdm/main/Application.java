package de.com.fdm.main;

import de.com.fdm.mongo.ConsumerRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"de.com.fdm.service", "de.com.fdm.tmi"})
@EnableMongoRepositories(basePackageClasses = ConsumerRepository.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
