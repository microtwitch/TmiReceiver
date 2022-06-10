package de.com.fdm.main

import de.com.fdm.core.LoadBalancer
import de.com.fdm.core.RedisListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling


@EnableScheduling
@SpringBootApplication(scanBasePackages = ["de.com.fdm.*"])
class Application @Autowired constructor(
    private val redisListener: RedisListener,
    private val loadBalancer: LoadBalancer,
) {
    private val log = LoggerFactory.getLogger(Application::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        loadBalancer.initReaders()
        redisListener.start()
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
