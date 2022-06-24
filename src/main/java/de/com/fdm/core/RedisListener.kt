package de.com.fdm.core

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

//TODO: offload channels if load gets too high over time
@Service
class RedisListener @Autowired constructor(
    private val loadBalancer: LoadBalancer,
) {
    private val log = LoggerFactory.getLogger(RedisListener::class.java)
    private val client: RedissonClient

    init {
        val config = Config.fromYAML(File("src/main/resources/redisson_config.yaml"))
        this.client = Redisson.create(config)
    }

    fun start() {
        val topic = client.getTopic("tmiReceiver")
        topic.addListener(String::class.java) { _: CharSequence, channel: String ->
            loadBalancer.joinChannel(channel)
        }
    }
}
