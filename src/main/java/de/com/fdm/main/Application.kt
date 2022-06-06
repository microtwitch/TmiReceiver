package de.com.fdm.main

import de.com.fdm.redis.RedisListener
import de.com.fdm.tmi.Reader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener


@SpringBootApplication(scanBasePackages = ["de.com.fdm.*"])
class Application @Autowired constructor(val reader: Reader, val redisListener: RedisListener) {
    private val log = LoggerFactory.getLogger(Application::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        log.info("Joining saved channels...")
        reader.joinSavedChannels()
        redisListener.setUpCallback()
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
