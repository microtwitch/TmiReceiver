package de.com.fdm.core

import com.github.twitch4j.chat.TwitchChat
import com.github.twitch4j.chat.TwitchChatBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageActionEvent
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class Reader constructor(
    private val deduplicator: Deduplicator,
    meterRegistry: MeterRegistry,
) {
    private val log = LoggerFactory.getLogger(Reader::class.java)
    private val id = UUID.randomUUID()
    private val twitchChat: TwitchChat = TwitchChatBuilder.builder().build()
    private val recentMessages = HashMap<Instant, String>()
    private val timer = Timer.builder("tmiReceiver.handleMessage.timer")
        .description("Times how long it takes to handle message received from twitch")
        .tag("reader", id.toString())
        .register(meterRegistry)
    private var whenLastJoin: Instant
    private val channelGauge = meterRegistry.gauge("tmiReceiver.channels.gauge", Tags.of("reader", id.toString()), AtomicInteger(0))!!

    init {
        whenLastJoin = Instant.now()
        twitchChat.eventManager.onEvent(ChannelMessageEvent::class.java) {
                event: ChannelMessageEvent -> timer.record { handleMessage(event) }
        }

        twitchChat.eventManager.onEvent(ChannelMessageActionEvent::class.java) {
                event: ChannelMessageActionEvent -> handleActionMessage(event)
        }
    }

    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    fun updateChannelGauge() {
        channelGauge.set(twitchChat.channels.size)
    }

    private fun handleMessage(event: ChannelMessageEvent) {
        val it = recentMessages.entries.iterator()
        while (it.hasNext()) {
            if (Duration.between(it.next().key, Instant.now()).toMillis() > 5000) {
                it.remove()
            }
        }

        recentMessages[Instant.now()] = event.messageEvent.messageId.get()

        val twitchMessageDto = TwitchMessageDto(event)
        deduplicator.handleMessage(twitchMessageDto)
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

    fun joinChannel(channel: String) {
        twitchChat.joinChannel(channel)
        whenLastJoin = Instant.now()
    }

    fun hasCapacity() : Boolean {
        if ((recentMessages.size/5) < 100 &&  timeSinceLastJoin() > 5000) {
            return true
        }

        return false
    }

    fun readsChannel(channel: String) : Boolean {
        return twitchChat.channels.contains(channel)
    }

    private fun timeSinceLastJoin() : Long {
        return Duration.between(whenLastJoin, Instant.now()).toMillis()
    }
}
