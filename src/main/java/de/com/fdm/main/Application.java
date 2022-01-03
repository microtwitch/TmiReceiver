package de.com.fdm.main;

import de.com.fdm.db.services.ChannelService;
import de.com.fdm.tmi.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "de.com.fdm.*")
@EnableJpaRepositories(basePackages = "de.com.fdm.db.repositories")
@EntityScan(basePackages = "de.com.fdm.db.data")
public class Application {

    @Autowired
    private Reader reader;

    @Autowired
    private ChannelService channelService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void readInitialChannels() {
        this.reader.joinChannels(this.channelService.getAll());
    }
}
