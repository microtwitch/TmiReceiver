package de.com.fdm.tmi

import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.TwitchChatBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import de.com.fdm.redis.RedisListener
import de.com.fdm.redis.RedisService
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.Queue
import java.util.concurrent.ArrayBlockingQueue

@Component
class Reader @Autowired constructor(
    private val redisService: RedisService,
    meterRegistry: MeterRegistry,
    @param:Value("\${inactivityThreshold}") private val inactivityThreshold: Int
) {
    private val log = LoggerFactory.getLogger(RedisListener::class.java)
    private val client: RedissonClient
    private val twitchChat: TwitchChat
    private val channelUsageHistory = HashMap<String, Queue<Long>>()
    private val timer = Timer.builder("tmiReceiver.handleMessage.timer")
                                    .description("Times how long it takes to handle message received from twitch")
                                    .register(meterRegistry)

    init {
        val config = Config.fromYAML(File("src/main/resources/redisson_config.yaml"))
        this.client = Redisson.create(config)

        this.twitchChat = TwitchChatBuilder.builder().build()
        twitchChat.eventManager.onEvent(ChannelMessageEvent::class.java) {
                event: ChannelMessageEvent -> timer.record { handleMessage(event) }
        }

        twitchChat.eventManager.onEvent(ChannelMessageActionEvent::class.java) {
                event: ChannelMessageActionEvent -> handleActionMessage(event)
        }
    }

    private fun handleMessage(event: ChannelMessageEvent) {
        val twitchMessageDto = TwitchMessageDto(event)
        val channel = twitchMessageDto.channel
        if (!isChannelUsed(channel)) {
            log.info("Channel #{} is not used.", channel)
            leaveChannel(channel)
            return
        }
        val numClients = redisService.publishMessage(twitchMessageDto)
        if (channelUsageHistory[channel]!!.size == inactivityThreshold) {
            channelUsageHistory[channel]!!.remove()
        }
        channelUsageHistory[channel]!!.add(numClients)
    }

    private fun handleActionMessage(event: ChannelMessageActionEvent) {
        val messageEvent = ChannelMessageEvent(
            event.channel,
            event.messageEvent,
            event.user,
            event.message,
            event.permissions
        )
        handleMessage(messageEvent)
    }

    private fun isChannelUsed(channel: String): Boolean {
        if (channelUsageHistory[channel]!!.size != inactivityThreshold) {
            return true
        }
        for (entry in channelUsageHistory[channel]!!) {
            if (entry != 0L) {
                return true
            }
        }
        return false
    }

    // twitch4j handles joining the same chat multiple times
    // the join gets ignored in that case!
    fun joinChannel(channel: String) {
        channelUsageHistory[channel] = ArrayBlockingQueue(inactivityThreshold)
        redisService.addChannel(channel)
        twitchChat.joinChannel(channel)

        log.info("Joined channel #{}.", channel)
    }

    private fun leaveChannel(channel: String) {
        redisService.removeChannel(channel)
        twitchChat.leaveChannel(channel)
        log.info("Left channel #{}.", channel)
    }

    fun joinSavedChannels() {
        val channels = redisService.getChannels()
        for (channel in channels) {
            joinChannel(channel)
        }
    }
}