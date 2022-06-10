package de.com.fdm.core

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent

data class TwitchMessageDto(
    val channel: String,
    val message: String,
    val userId: String,
    val rawMsg: String,
    val msgId: String,
) {
    constructor(event: ChannelMessageEvent) : this(
        event.channel.name,
        event.message,
        event.user.id,
        event.messageEvent.rawMessage,
        event.messageEvent.messageId.get(),
    )
}
