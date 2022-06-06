package de.com.fdm.redis

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import de.com.fdm.tmi.TwitchMessageDto
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class RedisService {
    private val log = LoggerFactory.getLogger(RedisListener::class.java)
    private val channelsKey = "channels"
    private val client: RedissonClient
    private val mapper = ObjectMapper()

    init {
        val config = Config.fromYAML(File("src/main/resources/redisson_config.yaml"))
        this.client = Redisson.create(config)
    }


    fun getChannels(): Set<String> {
        val channels = client.getSet<String>(channelsKey)
        return channels.readAll()
    }

    fun addChannel(channel: String) {
        val channels = client.getSet<String>(channelsKey)
        channels.add(channel)
    }

    fun removeChannel(channel: String) {
        val channels = client.getSet<String>(channelsKey)
        channels.remove(channel)
    }

    fun publishMessage(twitchMessageDto: TwitchMessageDto): Long {
        val topic = client.getTopic("tmiReceiver." + twitchMessageDto.channel)
        try {
            return topic.publish(mapper.writeValueAsString(twitchMessageDto))
        } catch (e: JsonProcessingException) {
            log.error(e.message)
        }

        return -1
    }
}