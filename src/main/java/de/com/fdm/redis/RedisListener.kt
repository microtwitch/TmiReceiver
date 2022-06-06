package de.com.fdm.redis

import de.com.fdm.tmi.Reader
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

@Service
class RedisListener @Autowired constructor(private val reader: Reader) {
    private val log = LoggerFactory.getLogger(RedisListener::class.java)
    private val client: RedissonClient

    init {
        val config = Config.fromYAML(File("src/main/resources/redisson_config.yaml"))
        this.client = Redisson.create(config)
    }

    fun setUpCallback() {
        val topic = client.getTopic("tmiReceiver")
        topic.addListener(String::class.java) { _: CharSequence, channel: String ->
            log.info("Received channel request: #{}", channel)
            reader.joinChannel(channel)
        }
    }
}