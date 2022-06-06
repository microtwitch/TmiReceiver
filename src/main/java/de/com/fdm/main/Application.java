package de.com.fdm.main;

import de.com.fdm.redis.RedisListener;
import de.com.fdm.tmi.Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


@SpringBootApplication(scanBasePackages = "de.com.fdm.*")
public class Application {
    private final Reader reader;
    private final RedisListener redisListener;

    @Autowired
    public Application(Reader reader, RedisListener redisListener) {
        this.reader = reader;
        this.redisListener = redisListener;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    private void joinSavedChannels() {
        reader.joinSavedChannels();

        redisListener.setUpCallback();
    }
}
