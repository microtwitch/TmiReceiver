package de.com.fdm.core

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class Deduplicator {
    private val log = LoggerFactory.getLogger(Deduplicator::class.java)
    private val client: RedissonClient
    private val mapper = ObjectMapper()

    init {
        val config = Config.fromYAML(File("src/main/resources/redisson_config.yaml"))
        this.client = Redisson.create(config)
    }

    fun handleMessage(msg: TwitchMessage) {
        val storedMsg = client.getSet<String>("messages")
        if (storedMsg.contains(msg.tags.getValue("id"))) {
            return;
        }

        storedMsg.add(msg.tags.getValue("id"))

        val topic = client.getTopic("tmiReceiver." + msg.channel)
        try {
            topic.publish(mapper.writeValueAsString(msg))
        } catch (e: JsonProcessingException) {
            log.error(e.message)
        }
    }
}
